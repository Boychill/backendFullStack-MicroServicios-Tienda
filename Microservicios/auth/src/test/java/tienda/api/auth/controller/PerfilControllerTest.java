package tienda.api.auth.controller;

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
import tienda.api.auth.dto.DireccionResponseDto;
import tienda.api.auth.model.Direccion;
import tienda.api.auth.service.PerfilService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilControllerTest {

    @Mock
    private PerfilService perfilService;

    @InjectMocks
    private PerfilController perfilController;

    private Faker faker;
    private Long mockUserId = 1L;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                mockUserId.toString(), "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
        SecurityContextHolder.setContext(context);
    }

    private Direccion createMockDireccion(Long id) {
        Direccion dir = new Direccion();
        dir.setId(id);
        dir.setUsuarioId(mockUserId);
        dir.setAlias("Casa");
        dir.setDireccionEscrita(faker.address().streetAddress());
        dir.setLatitud(10.5);
        dir.setLongitud(-66.9);
        dir.setEsPrincipal(true);
        return dir;
    }

    @Test
    void obtenerDireccion_Success() {
        Direccion dir = createMockDireccion(10L);
        when(perfilService.obtenerDireccion(mockUserId, 10L)).thenReturn(dir);

        ResponseEntity<?> response = perfilController.obtenerDireccion(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof DireccionResponseDto);
        assertEquals(10L, ((DireccionResponseDto) response.getBody()).getId());
    }

    @Test
    void misDirecciones_Success() {
        List<Direccion> direcciones = List.of(createMockDireccion(10L), createMockDireccion(11L));
        when(perfilService.listarMisDirecciones(mockUserId)).thenReturn(direcciones);

        ResponseEntity<List<DireccionResponseDto>> response = perfilController.misDirecciones();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void guardarDireccion_Success() {
        Direccion requestDir = createMockDireccion(null);
        Direccion savedDir = requestDir;
        savedDir.setId(15L);
        when(perfilService.agregarDireccion(eq(mockUserId), any(Direccion.class))).thenReturn(savedDir);

        ResponseEntity<?> response = perfilController.guardarDireccion(requestDir);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof DireccionResponseDto);
        assertEquals(15L, ((DireccionResponseDto) response.getBody()).getId());
    }

    @Test
    void eliminarDireccion_Success() {
        doNothing().when(perfilService).eliminarDireccion(mockUserId, 10L);

        ResponseEntity<?> response = perfilController.eliminarDireccion(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertEquals("Direccion borrada exitosamente", ((Map<?, ?>) response.getBody()).get("mensaje"));
    }
}
