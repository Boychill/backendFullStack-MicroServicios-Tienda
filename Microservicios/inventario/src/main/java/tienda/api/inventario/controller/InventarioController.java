package tienda.api.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.api.inventario.dto.*;
import tienda.api.inventario.model.AuditoriaStock;
import tienda.api.inventario.model.Bodega;
import tienda.api.inventario.service.InventarioService;
import tienda.api.inventario.repository.AuditoriaStockRepository;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Endpoints para la gestión de bodegas y stock físico")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private AuditoriaStockRepository auditoriaStockRepository;

    @GetMapping("/bodegas")
    @Operation(summary = "Listar bodegas", description = "Obtiene una lista de todas las bodegas físicas.")
    @ApiResponse(responseCode = "200", description = "Lista de bodegas obtenida exitosamente")
    public ResponseEntity<CollectionModel<BodegaResponseDto>> listarBodegas() {
        List<Bodega> bodegas = inventarioService.listarBodegas();
        
        List<BodegaResponseDto> dtos = bodegas.stream().map(this::convertBodegaToDto).collect(Collectors.toList());
        CollectionModel<BodegaResponseDto> collectionModel = CollectionModel.of(dtos);
        collectionModel.add(linkTo(methodOn(InventarioController.class).listarBodegas()).withSelfRel());
        
        return ResponseEntity.ok(collectionModel);
    }

    @PostMapping("/bodegas")
    @Operation(summary = "Crear bodega", description = "Crea una nueva bodega física.")
    @ApiResponse(responseCode = "200", description = "Bodega creada exitosamente")
    public ResponseEntity<BodegaResponseDto> crearBodega(@Valid @RequestBody Bodega bodega) {
        Bodega creada = inventarioService.crearBodega(bodega);
        return ResponseEntity.ok(convertBodegaToDto(creada));
    }

    @PostMapping("/ingreso")
    @Operation(summary = "Agregar inventario", description = "Añade unidades físicas a un producto dentro de una bodega específica.")
    public ResponseEntity<?> agregarInventario(@Valid @RequestBody IngresoRequest request) {
        try {
            inventarioService.registrarIngreso(request);
            return ResponseEntity.ok(Map.of("mensaje", "Inventario físico asignado a bodega exitosamente",
                    "_links", Map.of("auditoria", linkTo(methodOn(InventarioController.class).verAuditoria(request.getProductoId())).withRel("auditoria"))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/descuento")
    @Operation(summary = "Descontar stock", description = "Aplica un descuento de stock físico.")
    public ResponseEntity<?> aplicarDescuento(@Valid @RequestBody DescuentoRequest request) {
        try {
            String msj = inventarioService.descontarStock(request);
            return ResponseEntity.ok(Map.of("mensaje", msj));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/descuento-lote")
    @Operation(summary = "Descontar lote de stock", description = "Descuenta múltiples productos para una orden en específico.")
    public ResponseEntity<?> aplicarDescuentoLote(@RequestBody Map<String, Object> request) {
        try {
            java.util.List<Map<String, Object>> items = (java.util.List<Map<String, Object>>) request.get("items");
            Long ordenId = Long.parseLong(request.get("ordenId").toString());
            String msj = inventarioService.descontarStockLote(items, ordenId);
            return ResponseEntity.ok(Map.of("mensaje", msj));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/revertir")
    @Operation(summary = "Revertir descuento", description = "Revierte un descuento de stock asociado a una transacción o pedido.")
    public ResponseEntity<?> revertirDescuento(@Valid @RequestBody ReversionRequest request) {
        try {
            String msj = inventarioService.revertirDescuento(request);
            return ResponseEntity.ok(Map.of("mensaje", msj));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/auditoria/{productoId}")
    @Operation(summary = "Ver auditoría de producto", description = "Consulta el historial de movimientos de un producto.")
    public ResponseEntity<CollectionModel<AuditoriaStockResponseDto>> verAuditoria(@PathVariable Long productoId) {
        List<AuditoriaStock> auditorias = auditoriaStockRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId);
        List<AuditoriaStockResponseDto> dtos = auditorias.stream().map(this::convertAuditoriaToDto).collect(Collectors.toList());
        
        CollectionModel<AuditoriaStockResponseDto> collectionModel = CollectionModel.of(dtos);
        collectionModel.add(linkTo(methodOn(InventarioController.class).verAuditoria(productoId)).withSelfRel());
        
        return ResponseEntity.ok(collectionModel);
    }

    @PutMapping("/productos/{id}/estado")
    @Operation(summary = "Cambiar estado de producto en inventario")
    public ResponseEntity<?> estadoProducto(@PathVariable Long id, @RequestParam Boolean activo) {
        inventarioService.desactivarProducto(id, activo);
        return ResponseEntity.ok(Map.of("mensaje", "Estado del inventario actualizado correctamente"));
    }
    
    private BodegaResponseDto convertBodegaToDto(Bodega bodega) {
        BodegaResponseDto dto = BodegaResponseDto.builder()
                .id(bodega.getId())
                .nombre(bodega.getNombre())
                .ubicacion(bodega.getUbicacion())
                .activo(bodega.getActivo())
                .build();
        dto.add(linkTo(methodOn(InventarioController.class).listarBodegas()).withRel("bodegas"));
        return dto;
    }

    private AuditoriaStockResponseDto convertAuditoriaToDto(AuditoriaStock auditoria) {
        AuditoriaStockResponseDto dto = AuditoriaStockResponseDto.builder()
                .id(auditoria.getId())
                .productoId(auditoria.getProductoId())
                .bodegaId(auditoria.getBodegaId())
                .cantidadAfectada(auditoria.getCantidadAfectada())
                .tipoMovimiento(auditoria.getTipoMovimiento().name())
                .motivoReferencia(auditoria.getMotivoReferencia())
                .fechaMovimiento(auditoria.getFechaMovimiento())
                .responsableId(auditoria.getResponsableId())
                .build();
        dto.add(linkTo(methodOn(InventarioController.class).verAuditoria(auditoria.getProductoId())).withRel("auditoria_producto"));
        return dto;
    }
}
