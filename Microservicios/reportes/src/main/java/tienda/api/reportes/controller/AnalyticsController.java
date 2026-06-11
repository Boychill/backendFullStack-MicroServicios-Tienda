package tienda.api.reportes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tienda.api.reportes.dto.VentasAnalyticsDto;
import tienda.api.reportes.service.AnalyticsService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes y Analítica", description = "Endpoints para extraer métricas financieras y operativas globales")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/ventas")
    @Operation(summary = "Obtener Ventas Globales", description = "Calcula los totales de ventas y cantidades de productos vendidos consultando a los demás microservicios")
    @ApiResponse(responseCode = "200", description = "Métricas generadas exitosamente")
    @ApiResponse(responseCode = "500", description = "Error interno o de comunicación con otros servicios")
    public ResponseEntity<?> obtenerVentas() {
        try {
            VentasAnalyticsDto dto = analyticsService.calcularVentas();
            dto.add(linkTo(methodOn(AnalyticsController.class).obtenerVentas()).withSelfRel());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("error", "No se pudieron obtener las analíticas: " + e.getMessage()));
        }
    }
}
