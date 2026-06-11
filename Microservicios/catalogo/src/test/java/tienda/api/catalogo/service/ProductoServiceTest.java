package tienda.api.catalogo.service;

import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tienda.api.catalogo.client.InventarioClient;
import tienda.api.catalogo.dto.ProductoRequestDto;
import tienda.api.catalogo.model.Producto;
import tienda.api.catalogo.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InventarioClient inventarioClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ProductoService productoService;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    void obtenerTodos_RetornaLista() {
        Producto p1 = Producto.builder().id(1L).nombre(faker.commerce().productName()).activo(true).build();
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(p1));

        List<Producto> resultado = productoService.obtenerTodos(null);

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        verify(productoRepository).findByActivoTrue();
    }

    @Test
    void obenterPorId_RetornaProducto() {
        Producto p1 = Producto.builder().id(1L).nombre(faker.commerce().productName()).activo(true).build();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p1));

        Optional<Producto> resultado = productoService.obenterPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
    }

    @Test
    void guardar_GuardaYRetornaProducto() {
        ProductoRequestDto dto = new ProductoRequestDto(
                faker.commerce().productName(),
                faker.commerce().material(),
                new BigDecimal(faker.commerce().price().replace(",", ".")),
                faker.commerce().department(),
                faker.internet().image()
        );

        Producto productoGuardado = Producto.builder()
                .id(1L)
                .nombre(dto.getNombre())
                .precio(dto.getPrecio())
                .stock(0)
                .activo(true)
                .build();

        when(productoRepository.save(any(Producto.class))).thenReturn(productoGuardado);

        Producto resultado = productoService.guardar(dto);

        assertNotNull(resultado);
        assertEquals(dto.getNombre(), resultado.getNombre());
        assertEquals(0, resultado.getStock());
        verify(productoRepository).save(any(Producto.class));
    }
}
