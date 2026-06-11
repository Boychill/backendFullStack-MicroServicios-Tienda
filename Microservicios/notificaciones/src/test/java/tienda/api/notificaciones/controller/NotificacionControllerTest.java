package tienda.api.notificaciones.controller;

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
import tienda.api.notificaciones.dto.NotificacionResponseDto;
import tienda.api.notificaciones.model.Notificacion;
import tienda.api.notificaciones.service.NotificacionService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private NotificacionController notificacionController;

    private Faker faker;
    private Long mockUserId = 10L;
    private String mockRole = "ROLE_USER";

    @BeforeEach
    void setUp() {
        faker = new Faker();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                mockUserId.toString(), "password", Collections.singletonList(new SimpleGrantedAuthority(mockRole))));
        SecurityContextHolder.setContext(context);
    }

    private Notificacion createMockNotificacion(Long id, boolean leida) {
        return Notificacion.builder()
                .id(id)
                .receptorId(mockUserId)
                .rolReceptor(mockRole)
                .tipo("COMPRA_CONFIRMADA")
                .mensaje(faker.lorem().sentence())
                .leida(leida)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Test
    void obtenerMisNotificaciones_Success() {
        when(notificacionService.obtenerMisNotificaciones(mockUserId, mockRole))
                .thenReturn(List.of(createMockNotificacion(1L, false), createMockNotificacion(2L, true)));

        ResponseEntity<List<NotificacionResponseDto>> response = notificacionController.obtenerMisNotificaciones();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        
        // Unread notification should have HATEOAS link, read notification should not.
        assertTrue(response.getBody().get(0).hasLink("marcar_leida"));
        assertFalse(response.getBody().get(1).hasLink("marcar_leida"));
    }

    @Test
    void marcarComoLeida_Success() {
        doNothing().when(notificacionService).marcarComoLeida(1L, mockUserId);

        ResponseEntity<?> response = notificacionController.marcarComoLeida(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertEquals("Notificación marcada como leída", ((Map<?, ?>) response.getBody()).get("mensaje"));
    }
}
