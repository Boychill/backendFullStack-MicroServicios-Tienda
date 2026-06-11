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
import tienda.api.auth.dto.AuthRequest;
import tienda.api.auth.dto.AuthResponse;
import tienda.api.auth.dto.UsuarioDto;
import tienda.api.auth.repository.UsuarioRepository;
import tienda.api.auth.service.AuthService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthController authController;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    void register_Success() {
        AuthRequest request = new AuthRequest();
        request.setEmail(faker.internet().emailAddress());
        request.setPassword(faker.internet().password());
        when(authService.register(any(AuthRequest.class))).thenReturn("Usuario registrado exitosamente.");

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        verify(authService, times(1)).register(any(AuthRequest.class));
    }

    @Test
    void register_Error() {
        AuthRequest request = new AuthRequest();
        request.setEmail(faker.internet().emailAddress());
        request.setPassword(faker.internet().password());
        when(authService.register(any(AuthRequest.class))).thenReturn("El correo electrónico ya está registrado.");

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertEquals("El correo electrónico ya está registrado.", ((Map<?, ?>) response.getBody()).get("error"));
        verify(authService, times(1)).register(any(AuthRequest.class));
    }

    @Test
    void login_Success() {
        AuthRequest request = new AuthRequest();
        request.setEmail(faker.internet().emailAddress());
        request.setPassword(faker.internet().password());
        
        UsuarioDto userDto = new UsuarioDto();
        userDto.setEmail(request.getEmail());
        
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken("mock-token");
        authResponse.setType("Bearer");
        authResponse.setUsuario(userDto);
        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        assertEquals("mock-token", ((AuthResponse) response.getBody()).getToken());
        verify(authService, times(1)).login(any(AuthRequest.class));
    }

    @Test
    void countUsers_Success() {
        when(usuarioRepository.count()).thenReturn(150L);

        ResponseEntity<?> response = authController.countUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertEquals(150L, ((Map<?, ?>) response.getBody()).get("total"));
    }
}
