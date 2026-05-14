package tienda.api.inventario.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.api.inventario.dto.DescuentoRequest;
import tienda.api.inventario.dto.IngresoRequest;
import tienda.api.inventario.dto.ReversionRequest;
import tienda.api.inventario.model.Bodega;
import tienda.api.inventario.service.InventarioService;
import tienda.api.inventario.repository.AuditoriaStockRepository;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @GetMapping("/bodegas")
    public ResponseEntity<?> listarBodegas() {
        return ResponseEntity.ok(inventarioService.listarBodegas());
    }

    @PostMapping("/bodegas")
    public ResponseEntity<?> crearBodega(@Valid @RequestBody Bodega bodega) {
        return ResponseEntity.ok(inventarioService.crearBodega(bodega));
    }

    @PostMapping("/ingreso")
    public ResponseEntity<?> agregarInventario(@Valid @RequestBody IngresoRequest request) {
        try {
            inventarioService.registrarIngreso(request);
            return ResponseEntity.ok(Map.of("mensaje", "Inventario físico asignado a bodega exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/descuento")
    public ResponseEntity<?> aplicarDescuento(@Valid @RequestBody DescuentoRequest request) {
        try {
            String msj = inventarioService.descontarStock(request);
            return ResponseEntity.ok(Map.of("mensaje", msj));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/revertir")
    public ResponseEntity<?> revertirDescuento(@Valid @RequestBody ReversionRequest request) {
        try {
            String msj = inventarioService.revertirDescuento(request);
            return ResponseEntity.ok(Map.of("mensaje", msj));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @Autowired
    private AuditoriaStockRepository auditoriaStockRepository;

    @GetMapping("/auditoria/{productoId}")
    public ResponseEntity<?> verAuditoria(@PathVariable Long productoId) {
        return ResponseEntity.ok(auditoriaStockRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId));
    }

    @PutMapping("/productos/{id}/estado")
    public ResponseEntity<?> estadoProducto(@PathVariable Long id, @RequestParam Boolean activo) {
        inventarioService.desactivarProducto(id, activo);
        return ResponseEntity.ok(Map.of("mensaje", "Estado del inventario actualizado correctamente"));
    }
}
