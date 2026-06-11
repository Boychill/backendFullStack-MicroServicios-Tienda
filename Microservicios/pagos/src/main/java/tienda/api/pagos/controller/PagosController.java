package tienda.api.pagos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import tienda.api.pagos.dto.PagoRequest;
import tienda.api.pagos.dto.ProcesarPagoResponseDto;
import tienda.api.pagos.dto.ReembolsoResponseDto;
import tienda.api.pagos.model.Pago;
import tienda.api.pagos.repository.PagoRepository;

import java.util.Map;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Endpoints para la gestión de pagos y reembolsos")
public class PagosController {

    @Autowired
    private PagoRepository pagoRepository;
    
    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @PostMapping("/procesar")
    @Operation(summary = "Procesar pago", description = "Procesa una solicitud de pago y retorna la transacción y su estado")
    @ApiResponse(responseCode = "200", description = "Pago aprobado exitosamente")
    @ApiResponse(responseCode = "400", description = "Monto inválido")
    @ApiResponse(responseCode = "402", description = "Tarjeta declinada")
    public ResponseEntity<?> procesarPago(@RequestBody PagoRequest request) {
        if (request.getMontoTotal() == null || request.getMontoTotal().doubleValue() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Monto invalido"));
        }
        if (request.getNumeroTarjeta() == null || request.getNumeroTarjeta().length() < 12) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(Map.of("status", "RECHAZADO", "motivo", "Tarjeta declinada"));
        }
        
        String transactionId = UUID.randomUUID().toString();
        
        Pago pago = new Pago();
        pago.setMontoTotal(request.getMontoTotal());
        pago.setPedidoId(request.getPedidoId());
        pago.setEstado("APROBADO");
        pago.setTransaccionId(transactionId);
        pago.setFechaTransaccion(LocalDateTime.now());
        pagoRepository.save(pago);

        ProcesarPagoResponseDto response = ProcesarPagoResponseDto.builder()
            .status("APROBADO")
            .transactionId(transactionId)
            .mensaje("Pago procesado exitosamente")
            .build();
            
        response.add(linkTo(methodOn(PagosController.class).procesarReembolso(request.getPedidoId())).withRel("reembolso"));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reembolso/{pedidoId}")
    @Operation(summary = "Procesar reembolso", description = "Emite un reembolso total o parcial para el pedido a través de eventos RabbitMQ")
    @ApiResponse(responseCode = "200", description = "Reembolso encolado exitosamente")
    public ResponseEntity<ReembolsoResponseDto> procesarReembolso(@PathVariable Long pedidoId) {
        String refundId = UUID.randomUUID().toString();
        
        rabbitTemplate.convertAndSend(
            tienda.api.pagos.config.RabbitMQConfig.EXCHANGE, 
            tienda.api.pagos.config.RabbitMQConfig.ROUTING_KEY_REEMBOLSO, 
            Map.of("pedidoId", pedidoId, "refundId", refundId)
        );

        ReembolsoResponseDto response = ReembolsoResponseDto.builder()
            .status("REEMBOLSADO")
            .refundId(refundId)
            .mensaje("Reembolso procesado exitosamente")
            .build();
            
        // Autoreferencia y sugerencias
        response.add(linkTo(methodOn(PagosController.class).procesarReembolso(pedidoId)).withSelfRel());

        return ResponseEntity.ok(response);
    }
}
