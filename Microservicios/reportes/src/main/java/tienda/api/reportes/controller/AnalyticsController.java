package tienda.api.reportes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tienda.api.reportes.service.AnalyticsService;

@RestController
@RequestMapping("/api/reportes")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/ventas")
    public ResponseEntity<?> obtenerVentas() {
        try {
            return ResponseEntity.ok(analyticsService.calcularVentas());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("error", "No se pudieron obtener las analíticas: " + e.getMessage()));
        }
    }
}
