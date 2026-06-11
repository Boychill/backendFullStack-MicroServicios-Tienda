package tienda.api.carrito.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import tienda.api.carrito.dto.AgregarItemRequest;
import tienda.api.carrito.dto.CarritoResponseDto;
import tienda.api.carrito.dto.CartItemResponseDto;
import tienda.api.carrito.model.Carrito;
import tienda.api.carrito.service.CarritoService;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/carrito")
@Tag(name = "Carrito", description = "Endpoints para la gestión del carrito de compras")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;
    
    private Long getUserIdFromToken() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private CarritoResponseDto convertToDto(Carrito carrito) {
        List<CartItemResponseDto> itemDtos = carrito.getItems().stream()
            .map(item -> CartItemResponseDto.builder()
                .id(item.getId())
                .productoId(item.getProductoId())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(item.getSubtotal())
                .build())
            .collect(Collectors.toList());

        CarritoResponseDto dto = CarritoResponseDto.builder()
            .id(carrito.getId())
            .usuarioId(carrito.getUsuarioId())
            .total(carrito.getTotal())
            .items(itemDtos)
            .build();
            
        dto.add(linkTo(methodOn(CarritoController.class).verCarrito()).withSelfRel());
        dto.add(linkTo(methodOn(CarritoController.class).vaciarCarrito()).withRel("vaciar"));
        return dto;
    }

    @GetMapping
    @Operation(summary = "Ver carrito", description = "Obtiene el carrito activo del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Carrito obtenido exitosamente")
    public ResponseEntity<CarritoResponseDto> verCarrito() {
        Long usuarioId = getUserIdFromToken();
        Carrito carrito = carritoService.obtenerCarrito(usuarioId);
        return ResponseEntity.ok(convertToDto(carrito));
    }

    @PostMapping("/items")
    @Operation(summary = "Agregar ítem al carrito", description = "Agrega un nuevo producto o incrementa su cantidad")
    public ResponseEntity<CarritoResponseDto> agregarItem(@Valid @RequestBody AgregarItemRequest request) {
        Long usuarioId = getUserIdFromToken();
        Carrito carrito = carritoService.agregarItem(usuarioId, request.getProductoId(), request.getCantidad());
        return ResponseEntity.ok(convertToDto(carrito));
    }

    @DeleteMapping("/vaciar")
    @Operation(summary = "Vaciar carrito", description = "Elimina todos los ítems del carrito actual")
    public ResponseEntity<?> vaciarCarrito() {
        Long usuarioId = getUserIdFromToken();
        carritoService.vaciarCarrito(usuarioId);
        return ResponseEntity.ok(Map.of(
            "mensaje", "Carrito vaciado",
            "_links", Map.of("ver_carrito", linkTo(methodOn(CarritoController.class).verCarrito()).withRel("ver_carrito"))
        ));
    }

    @PutMapping("/items/{productoId}/reducir")
    @Operation(summary = "Reducir cantidad de un ítem", description = "Reduce la cantidad de un producto específico")
    public ResponseEntity<CarritoResponseDto> reducirCantidad(@PathVariable Long productoId, @RequestParam Integer cantidad) {
        Long usuarioId = getUserIdFromToken();
        Carrito carrito = carritoService.reducirCantidadItem(usuarioId, productoId, cantidad);
        return ResponseEntity.ok(convertToDto(carrito));
    }

    @DeleteMapping("/items/{productoId}")
    @Operation(summary = "Eliminar ítem del carrito", description = "Elimina por completo un producto del carrito")
    public ResponseEntity<CarritoResponseDto> eliminarItem(@PathVariable Long productoId) {
        Long usuarioId = getUserIdFromToken();
        Carrito carrito = carritoService.eliminarItem(usuarioId, productoId);
        return ResponseEntity.ok(convertToDto(carrito));
    }
}
