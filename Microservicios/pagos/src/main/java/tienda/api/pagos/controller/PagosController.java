package tienda.api.pagos.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.api.pagos.dto.PagoRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
public class PagosController {

    @PostMapping("/procesar")
    public ResponseEntity<?> procesarPago(@RequestBody PagoRequest request) {
        if (request.getMontoTotal() == null || request.getMontoTotal().doubleValue() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Monto inválido"));
        }
        if (request.getNumeroTarjeta() == null || request.getNumeroTarjeta().length() < 12) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(Map.of("status", "RECHAZADO", "motivo", "Tarjeta declinada"));
        }
        
        String transactionId = java.util.UUID.randomUUID().toString();
        return ResponseEntity.ok(Map.of(
            "status", "APROBADO",
            "transactionId", transactionId,
            "mensaje", "Pago procesado exitosamente"
        ));
    }
}
