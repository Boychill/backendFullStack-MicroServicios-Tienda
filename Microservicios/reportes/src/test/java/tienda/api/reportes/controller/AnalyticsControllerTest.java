package tienda.api.reportes.controller;

import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tienda.api.reportes.dto.VentasAnalyticsDto;
import tienda.api.reportes.service.AnalyticsService;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    void obtenerVentas_Success() {
        VentasAnalyticsDto mockDto = VentasAnalyticsDto.builder()
                .totalPedidos(150)
                .ingresoNeto(new BigDecimal("50000.00"))
                .ventasPorFecha(Map.of("2023-10-01", new BigDecimal("15000.00")))
                .productoMasVendidoId(1L)
                .totalUsuariosRegistrados(500L)
                .build();

        when(analyticsService.calcularVentas()).thenReturn(mockDto);

        ResponseEntity<?> response = analyticsController.obtenerVentas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof VentasAnalyticsDto);
        VentasAnalyticsDto responseDto = (VentasAnalyticsDto) response.getBody();
        assertEquals(150, responseDto.getTotalPedidos());
        assertEquals(new BigDecimal("50000.00"), responseDto.getIngresoNeto());
        assertTrue(responseDto.hasLink("self"));
    }

    @Test
    void obtenerVentas_Error() {
        when(analyticsService.calcularVentas()).thenThrow(new RuntimeException("Fallo en la comunicación con pedidos"));

        ResponseEntity<?> response = analyticsController.obtenerVentas();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertEquals("No se pudieron obtener las analíticas: Fallo en la comunicación con pedidos", ((Map<?, ?>) response.getBody()).get("error"));
    }
}
