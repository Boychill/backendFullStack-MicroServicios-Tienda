package tienda.api.carrito.controller;

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
import tienda.api.carrito.dto.AgregarItemRequest;
import tienda.api.carrito.dto.CarritoResponseDto;
import tienda.api.carrito.model.Carrito;
import tienda.api.carrito.model.CartItem;
import tienda.api.carrito.service.CarritoService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoControllerTest {

    @Mock
    private CarritoService carritoService;

    @InjectMocks
    private CarritoController carritoController;

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

    private Carrito createMockCarrito() {
        Carrito carrito = new Carrito();
        carrito.setId(100L);
        carrito.setUsuarioId(mockUserId);
        carrito.setTotal(new BigDecimal("150.0"));

        CartItem item = new CartItem();
        item.setId(10L);
        item.setCarritoId(100L);
        item.setProductoId(500L);
        item.setCantidad(3);
        item.setPrecioUnitario(new BigDecimal("50.0"));
        item.setSubtotal(new BigDecimal("150.0"));

        carrito.setItems(List.of(item));
        return carrito;
    }

    @Test
    void verCarrito_Exitoso() {
        when(carritoService.obtenerCarrito(mockUserId)).thenReturn(createMockCarrito());

        ResponseEntity<CarritoResponseDto> response = carritoController.verCarrito();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockUserId, response.getBody().getUsuarioId());
        assertEquals(1, response.getBody().getItems().size());
        
        verify(carritoService, times(1)).obtenerCarrito(mockUserId);
    }

    @Test
    void agregarItem_Exitoso() {
        AgregarItemRequest request = new AgregarItemRequest();
        request.setProductoId(500L);
        request.setCantidad(2);

        when(carritoService.agregarItem(mockUserId, 500L, 2)).thenReturn(createMockCarrito());

        ResponseEntity<CarritoResponseDto> response = carritoController.agregarItem(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(carritoService, times(1)).agregarItem(mockUserId, 500L, 2);
    }

    @Test
    void vaciarCarrito_Exitoso() {
        doNothing().when(carritoService).vaciarCarrito(mockUserId);

        ResponseEntity<?> response = carritoController.vaciarCarrito();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Carrito vaciado", body.get("mensaje"));
        verify(carritoService, times(1)).vaciarCarrito(mockUserId);
    }

    @Test
    void reducirCantidad_Exitoso() {
        when(carritoService.reducirCantidadItem(mockUserId, 500L, 1)).thenReturn(createMockCarrito());

        ResponseEntity<CarritoResponseDto> response = carritoController.reducirCantidad(500L, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(carritoService, times(1)).reducirCantidadItem(mockUserId, 500L, 1);
    }

    @Test
    void eliminarItem_Exitoso() {
        when(carritoService.eliminarItem(mockUserId, 500L)).thenReturn(createMockCarrito());

        ResponseEntity<CarritoResponseDto> response = carritoController.eliminarItem(500L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(carritoService, times(1)).eliminarItem(mockUserId, 500L);
    }
}
