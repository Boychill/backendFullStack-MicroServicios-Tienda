package tienda.api.pedidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.api.pedidos.service.CheckoutService;
import tienda.api.pedidos.repository.PedidoRepository;
import tienda.api.pedidos.model.Pedido;
import tienda.api.pedidos.model.ItemPedido;
import tienda.api.pedidos.dto.CheckoutRequest;
import tienda.api.pedidos.dto.ItemCompra;
import tienda.api.pedidos.dto.PedidoResponseDto;
import tienda.api.pedidos.dto.ItemPedidoResponseDto;
import tienda.api.pedidos.client.InventarioClient;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Endpoints para la gestión de órdenes, checkout y devoluciones")
public class PedidoController {

    @Autowired
    private CheckoutService checkoutService;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private InventarioClient inventarioClient;
    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @GetMapping
    @Operation(summary = "Listar todos los pedidos", description = "Retorna una lista de todos los pedidos registrados")
    @ApiResponse(responseCode = "200", description = "Pedidos obtenidos exitosamente")
    public ResponseEntity<CollectionModel<PedidoResponseDto>> listarPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        List<PedidoResponseDto> dtos = pedidos.stream().map(this::convertPedidoToDto).collect(Collectors.toList());
        
        CollectionModel<PedidoResponseDto> collectionModel = CollectionModel.of(dtos);
        collectionModel.add(linkTo(methodOn(PedidoController.class).listarPedidos()).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }

    @PostMapping("/checkout")
    @Operation(summary = "Realizar Checkout", description = "Crea un nuevo pedido verificando stock y reservándolo")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest payload) {
        try {
            Pedido pedido = checkoutService.realizarCheckout(payload);
            PedidoResponseDto dto = convertPedidoToDto(pedido);
            return ResponseEntity.ok(Map.of("mensaje", "Compra Exitosa", "orden", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/devolucion")
    @Operation(summary = "Devolver pedido", description = "Procesa la devolución de un pedido PAGADO y retorna stock al inventario")
    public ResponseEntity<?> devolucion(@PathVariable Long id, @RequestBody List<ItemCompra> itemsADevolver) {
        try {
            Pedido pedido = pedidoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
            if (!"PAGADO".equals(pedido.getEstado())) {
                throw new RuntimeException("Solo se pueden devolver pedidos en estado PAGADO");
            }

            // Revertir inventario
            for (ItemCompra item : itemsADevolver) {
                inventarioClient.revertirDescuento(Map.of("productoId", item.getProductoId(), "cantidad",
                        item.getCantidad(), "ordenId", pedido.getId().toString()), "ROLE_ADMIN");
            }

            // Cambiar estado
            pedido.setEstado("REEMBOLSADO");
            pedidoRepository.save(pedido);

            // Emitir Evento
            try {
                rabbitTemplate.convertAndSend(tienda.api.pedidos.config.RabbitMQConfig.EXCHANGE, "pedidos.devolucion", 
                    Map.of("pedidoId", pedido.getId(), "email", pedido.getUsuarioId()));
            } catch (Exception ignored) {}

            return ResponseEntity.ok(Map.of("mensaje", "Devolución procesada y stock retornado a bodegas con éxito", 
                "_links", Map.of("mis_pedidos", linkTo(methodOn(PedidoController.class).misPedidos()).withRel("mis_pedidos"))));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/mis-pedidos")
    @Operation(summary = "Mis pedidos", description = "Retorna los pedidos asociados al usuario autenticado actual")
    public ResponseEntity<CollectionModel<PedidoResponseDto>> misPedidos() {
        Long usuarioId = Long.parseLong(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
        List<Pedido> pedidos = pedidoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        
        List<PedidoResponseDto> dtos = pedidos.stream().map(this::convertPedidoToDto).collect(Collectors.toList());
        CollectionModel<PedidoResponseDto> collectionModel = CollectionModel.of(dtos);
        collectionModel.add(linkTo(methodOn(PedidoController.class).misPedidos()).withSelfRel());
        
        return ResponseEntity.ok(collectionModel);
    }
    
    private PedidoResponseDto convertPedidoToDto(Pedido pedido) {
        List<ItemPedidoResponseDto> itemDtos = pedido.getItems().stream()
                .map(item -> ItemPedidoResponseDto.builder()
                        .id(item.getId())
                        .productoId(item.getProductoId())
                        .cantidad(item.getCantidad())
                        .precioUnitario(item.getPrecioUnitario())
                        .build())
                .collect(Collectors.toList());

        PedidoResponseDto dto = PedidoResponseDto.builder()
                .id(pedido.getId())
                .usuarioId(pedido.getUsuarioId())
                .total(pedido.getTotal())
                .estado(pedido.getEstado())
                .transaccionId(pedido.getTransaccionId())
                .fechaCreacion(pedido.getFechaCreacion())
                .direccionCompleta(pedido.getDireccionCompleta())
                .items(itemDtos)
                .build();
        
        dto.add(linkTo(methodOn(PedidoController.class).listarPedidos()).withRel("todos_los_pedidos"));
        return dto;
    }
}
