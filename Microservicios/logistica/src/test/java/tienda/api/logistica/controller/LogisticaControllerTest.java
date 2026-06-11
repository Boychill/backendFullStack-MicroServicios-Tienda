package tienda.api.logistica.controller;

import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import tienda.api.logistica.dto.GuiaDespachoResponseDto;
import tienda.api.logistica.model.GuiaDespacho;
import tienda.api.logistica.service.LogisticaService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogisticaControllerTest {

    @Mock
    private LogisticaService logisticaService;

    @InjectMocks
    private LogisticaController logisticaController;

    private Faker faker;
    private Long mockUserId = 5L;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                mockUserId.toString(), "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_CHOFER"))));
        SecurityContextHolder.setContext(context);
    }

    private GuiaDespacho createMockGuia(Long id, String estado) {
        return GuiaDespacho.builder()
                .id(id)
                .pedidoId(100L + id)
                .choferId(estado.equals("PENDIENTE") || estado.equals("ARMADO") ? null : mockUserId)
                .direccionCompleta(faker.address().fullAddress())
                .estado(estado)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Test
    void listarParaArmar_Success() {
        when(logisticaService.listarPendientes()).thenReturn(List.of(createMockGuia(1L, "PENDIENTE")));

        ResponseEntity<List<GuiaDespachoResponseDto>> response = logisticaController.listarParaArmar();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("PENDIENTE", response.getBody().get(0).getEstado());
    }

    @Test
    void marcarComoArmado_Success() {
        GuiaDespacho guia = createMockGuia(1L, "ARMADO");
        when(logisticaService.marcarComoArmado(1L)).thenReturn(guia);

        ResponseEntity<?> response = logisticaController.marcarComoArmado(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof GuiaDespachoResponseDto);
        assertEquals("ARMADO", ((GuiaDespachoResponseDto) response.getBody()).getEstado());
    }

    @Test
    void asignarChofer_Self_Success() {
        GuiaDespacho guia = createMockGuia(1L, "ASIGNADO");
        when(logisticaService.asignarChofer(1L, mockUserId)).thenReturn(guia);

        ResponseEntity<?> response = logisticaController.asignarChofer(1L, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof GuiaDespachoResponseDto);
        assertEquals("ASIGNADO", ((GuiaDespachoResponseDto) response.getBody()).getEstado());
    }

    @Test
    void actualizarEstado_Success() {
        GuiaDespacho guia = createMockGuia(1L, "ENTREGADO");
        when(logisticaService.actualizarEstado(1L, "ENTREGADO", mockUserId)).thenReturn(guia);

        ResponseEntity<?> response = logisticaController.actualizarEstado(1L, "ENTREGADO");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ENTREGADO", ((GuiaDespachoResponseDto) response.getBody()).getEstado());
    }

    @Test
    void listarTodasLasRutas_Forbidden() {
        // Current role is ROLE_CHOFER, not manager
        ResponseEntity<?> response = logisticaController.listarTodasLasRutas();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void listarTodasLasRutas_Manager_Success() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                mockUserId.toString(), "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        SecurityContextHolder.setContext(context);

        when(logisticaService.listarTodas()).thenReturn(List.of(createMockGuia(1L, "ASIGNADO")));

        ResponseEntity<?> response = logisticaController.listarTodasLasRutas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
