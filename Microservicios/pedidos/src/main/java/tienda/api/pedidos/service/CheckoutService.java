package tienda.api.pedidos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import tienda.api.pedidos.client.CarritoClient;
import tienda.api.pedidos.client.InventarioClient;
import tienda.api.pedidos.client.PagosClient;
import tienda.api.pedidos.client.AuthClient;
import tienda.api.pedidos.model.Pedido;
import tienda.api.pedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import tienda.api.pedidos.dto.ItemCompra;

@Service
public class CheckoutService {

    @Autowired private CarritoClient carritoClient;
    @Autowired private PagosClient pagosClient;
    @Autowired private InventarioClient inventarioClient;
    @Autowired private AuthClient authClient;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public Pedido realizarCheckout(tienda.api.pedidos.dto.CheckoutRequest request) {
        Long usuarioId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        String numeroTarjeta = request.getNumeroTarjeta();
        List<ItemCompra> productosSeleccionados = request.getProductosSeleccionados();
        String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();

        Map<String, Object> dirInfo = authClient.obtenerDireccion(request.getDireccionId(), role, usuarioId);
        String direccionStr = dirInfo.getOrDefault("direccionEscrita", "Direccion Desconocida").toString();

        Map<String, Object> carrito = carritoClient.obtenerCarrito(role, usuarioId);
        if (carrito == null || carrito.get("items") == null) throw new RuntimeException("Carrito vacio o invalido");

        List<Map<String, Object>> itemsCarrito = (List<Map<String, Object>>) carrito.get("items");
        if (itemsCarrito.isEmpty()) throw new RuntimeException("Carrito vacio");

        List<Map<String, Object>> itemsAProcesar = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        if (productosSeleccionados == null || productosSeleccionados.isEmpty()) {
            itemsAProcesar = itemsCarrito;
            total = new BigDecimal(carrito.get("total").toString());
        } else {
            for (ItemCompra seleccion : productosSeleccionados) {
                Long pIdSeleccionado = seleccion.getProductoId();
                Integer cantSeleccionada = seleccion.getCantidad();

                boolean encontrado = false;
                for (Map<String, Object> itemCar : itemsCarrito) {
                    Long pIdCar = Long.parseLong(itemCar.get("productoId").toString());
                    Integer cantCar = Integer.parseInt(itemCar.get("cantidad").toString());

                    if (pIdCar.equals(pIdSeleccionado)) {
                        if (cantSeleccionada > cantCar) {
                            throw new RuntimeException("No puedes comprar " + cantSeleccionada + " del producto " + pIdSeleccionado + " porque solo tienes " + cantCar + " en el carrito.");
                        }
                        BigDecimal precioUnitario = new BigDecimal(itemCar.get("precioUnitario").toString());
                        Map<String, Object> itemFiltrado = new HashMap<>(itemCar);
                        itemFiltrado.put("cantidad", cantSeleccionada);
                        itemsAProcesar.add(itemFiltrado);
                        total = total.add(precioUnitario.multiply(new BigDecimal(cantSeleccionada)));
                        encontrado = true;
                        break;
                    }
                }
                if (!encontrado) throw new RuntimeException("El producto " + pIdSeleccionado + " no esta en tu carrito.");
            }
        }

        Pedido pedido = new Pedido();
        pedido.setUsuarioId(usuarioId);
        pedido.setDireccionCompleta(direccionStr);
        pedido.setTotal(total);
        pedido.setEstado("PENDIENTE");
        pedido.setFechaCreacion(LocalDateTime.now());

        List<tienda.api.pedidos.model.ItemPedido> itemPedidos = new ArrayList<>();
        for (Map<String, Object> itemAProcesar : itemsAProcesar) {
            tienda.api.pedidos.model.ItemPedido ip = new tienda.api.pedidos.model.ItemPedido(
                Long.parseLong(itemAProcesar.get("productoId").toString()),
                Integer.parseInt(itemAProcesar.get("cantidad").toString()),
                new BigDecimal(itemAProcesar.get("precioUnitario").toString()),
                pedido
            );
            itemPedidos.add(ip);
        }
        pedido.setItems(itemPedidos);

        pedido = pedidoRepository.save(pedido);

        List<Map<String, Object>> itemsDescuento = new ArrayList<>();
        for (Map<String, Object> item : itemsAProcesar) {
            itemsDescuento.add(Map.of(
                "productoId", Long.parseLong(item.get("productoId").toString()),
                "cantidad", Integer.parseInt(item.get("cantidad").toString())
            ));
        }

        try {
            inventarioClient.descontarStockLote(Map.of("ordenId", pedido.getId(), "items", itemsDescuento), "ROLE_ADMIN");
        } catch (Exception e) {
            pedido.setEstado("FALLIDO_STOCK");
            pedidoRepository.save(pedido);
            throw new RuntimeException("Error descontando stock. Operacion abortada.");
        }

        try {
            Map<String, String> pagoRes = pagosClient.procesarPago(Map.of("numeroTarjeta", numeroTarjeta, "montoTotal", total, "pedidoId", pedido.getId()), role);
            if ("APROBADO".equals(pagoRes.get("status"))) {
                pedido.setEstado("PAGADO");
                pedido.setTransaccionId(pagoRes.get("transactionId"));
            } else {
                throw new RuntimeException("Pago rechazado por el proveedor");
            }
        } catch (Exception e) {
            pedido.setEstado("FALLIDO_PAGO");
            pedidoRepository.save(pedido);
            
            // SAGA ROLLBACK ASYNCHRONOUS
            rabbitTemplate.convertAndSend(tienda.api.pedidos.config.RabbitMQConfig.EXCHANGE, "pedidos.compensacion.stock", Map.of("ordenId", pedido.getId(), "items", itemsDescuento));
            
            throw new RuntimeException("El pago fallo. El inventario sera revertido asincronamente. Detalles: " + e.getMessage());
        }

        try {
            List<tienda.api.pedidos.event.PedidoPagadoEvent.ItemComprado> itemsEvt = new ArrayList<>();
            for (Map<String, Object> item : itemsAProcesar) {
                itemsEvt.add(new tienda.api.pedidos.event.PedidoPagadoEvent.ItemComprado(Long.parseLong(item.get("productoId").toString()), Integer.parseInt(item.get("cantidad").toString())));
            }
            tienda.api.pedidos.event.PedidoPagadoEvent event = new tienda.api.pedidos.event.PedidoPagadoEvent(pedido.getId(), usuarioId, direccionStr, itemsEvt);
            rabbitTemplate.convertAndSend(tienda.api.pedidos.config.RabbitMQConfig.EXCHANGE, tienda.api.pedidos.config.RabbitMQConfig.ROUTING_KEY_CARRITO, event);
            
            // PUBLICAR EVENTO A LOGISTICA
            rabbitTemplate.convertAndSend(tienda.api.pedidos.config.RabbitMQConfig.EXCHANGE, tienda.api.pedidos.config.RabbitMQConfig.ROUTING_KEY_LOGISTICA, Map.of(
                "pedidoId", pedido.getId(),
                "direccionCompleta", direccionStr
            ));
        } catch (Exception e) {
            System.err.println("ADVERTENCIA: Fallo notificacion de limpieza del carrito. Motivo: " + e.getMessage());
        }

        return pedidoRepository.save(pedido);
    }
}

