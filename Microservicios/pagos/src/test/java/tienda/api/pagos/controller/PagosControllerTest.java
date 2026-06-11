package tienda.api.pagos.controller;

import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tienda.api.pagos.dto.PagoRequest;
import tienda.api.pagos.dto.ProcesarPagoResponseDto;
import tienda.api.pagos.dto.ReembolsoResponseDto;
import tienda.api.pagos.model.Pago;
import tienda.api.pagos.repository.PagoRepository;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagosControllerTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PagosController pagosController;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    void procesarPago_Aprobado() {
        // Arrange
        PagoRequest request = new PagoRequest();
        request.setPedidoId(1L);
        request.setMontoTotal(new BigDecimal("150.0"));
        request.setNumeroTarjeta(faker.finance().creditCard()); // 16 digits

        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<?> responseEntity = pagosController.procesarPago(request);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof ProcesarPagoResponseDto);
        ProcesarPagoResponseDto dto = (ProcesarPagoResponseDto) responseEntity.getBody();
        assertEquals("APROBADO", dto.getStatus());
        assertNotNull(dto.getTransactionId());
        assertEquals("Pago procesado exitosamente", dto.getMensaje());
        verify(pagoRepository, times(1)).save(any(Pago.class));
    }

    @Test
    void procesarPago_MontoInvalido() {
        // Arrange
        PagoRequest request = new PagoRequest();
        request.setPedidoId(1L);
        request.setMontoTotal(new BigDecimal("-10.0")); // Inválido
        request.setNumeroTarjeta(faker.finance().creditCard());

        // Act
        ResponseEntity<?> responseEntity = pagosController.procesarPago(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertEquals("Monto invalido", body.get("error"));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void procesarPago_TarjetaRechazada() {
        // Arrange
        PagoRequest request = new PagoRequest();
        request.setPedidoId(1L);
        request.setMontoTotal(new BigDecimal("150.0"));
        request.setNumeroTarjeta("1234"); // Inválido, menos de 12

        // Act
        ResponseEntity<?> responseEntity = pagosController.procesarPago(request);

        // Assert
        assertEquals(HttpStatus.PAYMENT_REQUIRED, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertEquals("RECHAZADO", body.get("status"));
        assertEquals("Tarjeta declinada", body.get("motivo"));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void procesarReembolso_Exitoso() {
        // Act
        ResponseEntity<ReembolsoResponseDto> responseEntity = pagosController.procesarReembolso(1L);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ReembolsoResponseDto dto = responseEntity.getBody();
        assertNotNull(dto);
        assertEquals("REEMBOLSADO", dto.getStatus());
        assertNotNull(dto.getRefundId());
        
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyMap());
    }
}
