package tienda.api.pedidos.service;

import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import tienda.api.pedidos.client.AuthClient;
import tienda.api.pedidos.client.CarritoClient;
import tienda.api.pedidos.client.InventarioClient;
import tienda.api.pedidos.client.PagosClient;
import tienda.api.pedidos.dto.CheckoutRequest;
import tienda.api.pedidos.dto.ItemCompra;
import tienda.api.pedidos.model.Pedido;
import tienda.api.pedidos.repository.PedidoRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock private CarritoClient carritoClient;
    @Mock private PagosClient pagosClient;
    @Mock private InventarioClient inventarioClient;
    @Mock private AuthClient authClient;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CheckoutService checkoutService;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                "1", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
        SecurityContextHolder.setContext(context);
    }

    @Test
    void realizarCheckout_Exitoso() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setNumeroTarjeta(faker.finance().creditCard());
        request.setDireccionId(10L);
        ItemCompra seleccion = new ItemCompra();
        seleccion.setProductoId(100L);
        seleccion.setCantidad(2);
        request.setProductosSeleccionados(List.of(seleccion));

        when(authClient.obtenerDireccion(eq(10L), anyString(), eq(1L)))
                .thenReturn(Map.of("direccionEscrita", faker.address().fullAddress()));

        when(carritoClient.obtenerCarrito(anyString(), eq(1L)))
                .thenReturn(Map.of("items", List.of(
                        Map.of("productoId", 100L, "cantidad", 5, "precioUnitario", 50.0)
                ), "total", 250.0));

        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(1L);
            return p;
        });

        when(pagosClient.procesarPago(anyMap(), anyString()))
                .thenReturn(Map.of("status", "APROBADO", "transactionId", faker.internet().uuid()));

        // Act
        Pedido resultado = checkoutService.realizarCheckout(request);

        // Assert
        assertNotNull(resultado);
        assertEquals("PAGADO", resultado.getEstado());
        assertEquals(0, new BigDecimal("100.0").compareTo(resultado.getTotal()));
        assertNotNull(resultado.getTransaccionId());
        
        verify(inventarioClient).descontarStockLote(anyMap(), eq("ROLE_ADMIN"));
        verify(rabbitTemplate, atLeastOnce()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void realizarCheckout_FallaStock() {
        CheckoutRequest request = new CheckoutRequest();
        request.setDireccionId(10L);

        when(authClient.obtenerDireccion(anyLong(), anyString(), anyLong()))
                .thenReturn(Map.of("direccionEscrita", "Calle Falsa 123"));

        when(carritoClient.obtenerCarrito(anyString(), anyLong()))
                .thenReturn(Map.of("items", List.of(
                        Map.of("productoId", 100L, "cantidad", 1, "precioUnitario", 50.0)
                ), "total", 50.0));

        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(1L);
            return p;
        });

        doThrow(new RuntimeException("Stock Insuficiente"))
                .when(inventarioClient).descontarStockLote(anyMap(), eq("ROLE_ADMIN"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> checkoutService.realizarCheckout(request));
        assertEquals("Error descontando stock. Operacion abortada.", exception.getMessage());
    }
}
