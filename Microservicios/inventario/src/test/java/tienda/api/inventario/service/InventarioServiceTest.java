package tienda.api.inventario.service;

import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tienda.api.inventario.client.CatalogoClient;
import tienda.api.inventario.dto.IngresoRequest;
import tienda.api.inventario.model.Bodega;
import tienda.api.inventario.model.InventarioBodega;
import tienda.api.inventario.model.AuditoriaStock;
import tienda.api.inventario.repository.AuditoriaStockRepository;
import tienda.api.inventario.repository.BodegaRepository;
import tienda.api.inventario.repository.InventarioBodegaRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private BodegaRepository bodegaRepository;

    @Mock
    private InventarioBodegaRepository inventarioRepository;

    @Mock
    private AuditoriaStockRepository auditoriaStockRepository;

    @Mock
    private CatalogoClient catalogoClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private InventarioService inventarioService;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    void crearBodega_GuardaYRetornaBodega() {
        Bodega bodega = new Bodega();
        bodega.setNombre(faker.company().name());
        bodega.setUbicacion(faker.address().fullAddress());
        bodega.setActivo(true);

        Bodega guardada = new Bodega();
        guardada.setId(1L);
        guardada.setNombre(bodega.getNombre());
        guardada.setUbicacion(bodega.getUbicacion());
        guardada.setActivo(bodega.getActivo());

        when(bodegaRepository.save(any(Bodega.class))).thenReturn(guardada);

        Bodega resultado = inventarioService.crearBodega(bodega);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(bodega.getNombre(), resultado.getNombre());
        verify(bodegaRepository).save(any(Bodega.class));
    }

    @Test
    void registrarIngreso_Exitoso() {
        IngresoRequest req = new IngresoRequest();
        req.setBodegaId(1L);
        req.setProductoId(10L);
        req.setCantidadFisica(50);

        Bodega bodega = new Bodega();
        bodega.setId(1L);
        bodega.setNombre("Central");
        bodega.setUbicacion("Dir");
        bodega.setActivo(true);

        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));

        InventarioBodega invMock = new InventarioBodega();
        invMock.setBodegaId(1L);
        invMock.setProductoId(10L);
        invMock.setCantidadDisponible(0);

        when(inventarioRepository.findByBodegaIdAndProductoId(1L, 10L)).thenReturn(Optional.of(invMock));
        when(inventarioRepository.save(any(InventarioBodega.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventarioBodega resultado = inventarioService.registrarIngreso(req);

        assertNotNull(resultado);
        assertEquals(50, resultado.getCantidadDisponible());
        verify(auditoriaStockRepository).save(any(AuditoriaStock.class));
        verify(rabbitTemplate, atLeastOnce()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void registrarIngreso_FallaSiBodegaNoExiste() {
        IngresoRequest req = new IngresoRequest();
        req.setBodegaId(99L);
        req.setProductoId(10L);
        req.setCantidadFisica(50);

        when(bodegaRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> inventarioService.registrarIngreso(req));
        assertEquals("Bodega no existe", exception.getMessage());
    }
}
