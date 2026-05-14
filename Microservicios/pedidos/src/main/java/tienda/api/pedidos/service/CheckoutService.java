package tienda.api.pedidos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import tienda.api.pedidos.client.CarritoClient;
import tienda.api.pedidos.client.InventarioClient;
import tienda.api.pedidos.client.PagosClient;
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

    @Autowired
    private CarritoClient carritoClient;
    @Autowired
    private PagosClient pagosClient;
    @Autowired
    private InventarioClient inventarioClient;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public Pedido realizarCheckout(String numeroTarjeta,
            List<ItemCompra> productosSeleccionados) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next()
                .getAuthority();

        // 1. Obtener Carrito
        Map<String, Object> carrito = carritoClient.obtenerCarrito(role, email);
        if (carrito == null || carrito.get("items") == null)
            throw new RuntimeException("Carrito vacío o inválido");

        List<Map<String, Object>> itemsCarrito = (List<Map<String, Object>>) carrito.get("items");
        if (itemsCarrito.isEmpty())
            throw new RuntimeException("Carrito vacío");

        List<Map<String, Object>> itemsAProcesar = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // 2. Filtro Inteligente y Granular
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
                            throw new RuntimeException("No puedes comprar " + cantSeleccionada + " del producto "
                                    + pIdSeleccionado + " porque solo tienes " + cantCar + " en el carrito.");
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
                if (!encontrado)
                    throw new RuntimeException("El producto " + pIdSeleccionado + " no está en tu carrito.");
            }
        }

        // 3. Crear Pedido en estado PENDIENTE
        Pedido pedido = new Pedido();
        pedido.setUsuarioEmail(email);
        pedido.setTotal(total);
        pedido.setEstado("PENDIENTE");
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido = pedidoRepository.save(pedido);

        // 4. Descontar Inventario (Fail Fast)
        for (Map<String, Object> item : itemsAProcesar) {
            Long pId = Long.parseLong(item.get("productoId").toString());
            Integer canti = Integer.parseInt(item.get("cantidad").toString());
            try {
                inventarioClient.descontarStock(
                        Map.of("productoId", pId, "cantidadDescontar", canti, "ordenId", pedido.getId().toString()),
                        "ROLE_ADMIN");
            } catch (Exception e) {
                pedido.setEstado("FALLIDO_STOCK");
                pedidoRepository.save(pedido);
                throw new RuntimeException("Error descontando stock del producto " + pId + ". Operación abortada.");
            }
        }

        // 5. Cobrar Pago
        try {
            Map<String, String> pagoRes = pagosClient
                    .procesarPago(Map.of("numeroTarjeta", numeroTarjeta, "montoTotal", total), role);
            if ("APROBADO".equals(pagoRes.get("status"))) {
                pedido.setEstado("PAGADO");
                pedido.setTransaccionId(pagoRes.get("transactionId"));
            } else {
                throw new RuntimeException("Pago rechazado por el proveedor");
            }
        } catch (Exception e) {
            // SAGA ROLLBACK: El pago falló, debemos devolver el stock al inventario
            pedido.setEstado("FALLIDO_PAGO");
            pedidoRepository.save(pedido);
            
            for (Map<String, Object> item : itemsAProcesar) {
                try {
                    Long pId = Long.parseLong(item.get("productoId").toString());
                    Integer canti = Integer.parseInt(item.get("cantidad").toString());
                    inventarioClient.revertirDescuento(Map.of("productoId", pId, "cantidad", canti, "ordenId", pedido.getId().toString()), "ROLE_ADMIN");
                } catch(Exception exReversion) {
                    System.err.println("ALERTA CRÍTICA SAGA: Falló la reversión de inventario para la orden " + pedido.getId());
                }
            }
            
            throw new RuntimeException("El pago falló y el inventario fue revertido. Detalles: " + e.getMessage());
        }

        // 6. Consolidación: Limpieza Post-Compra
        try {
            List<tienda.api.pedidos.event.PedidoPagadoEvent.ItemComprado> itemsEvt = null;
            if (productosSeleccionados != null && !productosSeleccionados.isEmpty()) {
                itemsEvt = new ArrayList<>();
                for (ItemCompra sel : productosSeleccionados) {
                    itemsEvt.add(new tienda.api.pedidos.event.PedidoPagadoEvent.ItemComprado(sel.getProductoId(), sel.getCantidad()));
                }
            }
            tienda.api.pedidos.event.PedidoPagadoEvent event = new tienda.api.pedidos.event.PedidoPagadoEvent(email, itemsEvt);
            rabbitTemplate.convertAndSend(tienda.api.pedidos.config.RabbitMQConfig.EXCHANGE, tienda.api.pedidos.config.RabbitMQConfig.ROUTING_KEY_CARRITO, event);
            System.out.println("Evento PedidoPagadoEvent publicado para limpiar el carrito de: " + email);
        } catch (Exception e) {
            System.err.println("ADVERTENCIA: Falló la notificación de limpieza del carrito para " + email + ". La compra fue exitosa. Motivo: " + e.getMessage());
        }

        return pedidoRepository.save(pedido);
    }
}
