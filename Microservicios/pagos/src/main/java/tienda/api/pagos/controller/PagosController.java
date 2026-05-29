package tienda.api.pagos.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import tienda.api.pagos.dto.PagoRequest;
import tienda.api.pagos.model.Pago;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/pagos")
public class PagosController {

    @Autowired
    private tienda.api.pagos.repository.PagoRepository pagoRepository;
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @PostMapping("/procesar")
    public ResponseEntity<?> procesarPago(@RequestBody PagoRequest request) {
        if (request.getMontoTotal() == null || request.getMontoTotal().doubleValue() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Monto invalido"));
        }
        if (request.getNumeroTarjeta() == null || request.getNumeroTarjeta().length() < 12) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(Map.of("status", "RECHAZADO", "motivo", "Tarjeta declinada"));
        }
        
        String transactionId = java.util.UUID.randomUUID().toString();
        
        Pago pago = new Pago();
        pago.setMontoTotal(request.getMontoTotal());
        pago.setPedidoId(request.getPedidoId());
        pago.setEstado("APROBADO");
        pago.setTransaccionId(transactionId);
        pago.setFechaTransaccion(LocalDateTime.now());
        pagoRepository.save(pago);

        return ResponseEntity.ok(Map.of(
            "status", "APROBADO",
            "transactionId", transactionId,
            "mensaje", "Pago procesado exitosamente"
        ));
    }

    @PostMapping("/reembolso/{pedidoId}")
    public ResponseEntity<?> procesarReembolso(@PathVariable Long pedidoId) {
        String refundId = java.util.UUID.randomUUID().toString();
        
        rabbitTemplate.convertAndSend(
            tienda.api.pagos.config.RabbitMQConfig.EXCHANGE, 
            tienda.api.pagos.config.RabbitMQConfig.ROUTING_KEY_REEMBOLSO, 
            Map.of("pedidoId", pedidoId, "refundId", refundId)
        );

        return ResponseEntity.ok(Map.of(
            "status", "REEMBOLSADO",
            "refundId", refundId,
            "mensaje", "Reembolso procesado exitosamente"
        ));
    }
}

