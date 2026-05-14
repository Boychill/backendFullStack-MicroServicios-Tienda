package tienda.api.catalogo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.api.catalogo.model.Producto;
import tienda.api.catalogo.repository.ProductoRepository;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private tienda.api.catalogo.client.InventarioClient inventarioClient;

    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public List<Producto> obtenerTodos(String categoria) {
        if(categoria != null && !categoria.isEmpty()){
            return productoRepository.findByCategoriaAndActivoTrue(categoria);
        }
        return productoRepository.findByActivoTrue();
    }

    public Optional<Producto> obenterPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto guardar(tienda.api.catalogo.dto.ProductoRequestDto dto) {
        Producto producto = Producto.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .categoria(dto.getCategoria())
                .imagenUrl(dto.getImagenUrl())
                .stock(0) // FORZAR STOCK 0
                .activo(true)
                .build();
        return productoRepository.save(producto);
    }

    public void actualizarStock(Long id, Integer stock) {
        productoRepository.findById(id).ifPresent(producto -> {
            producto.setStock(stock);
            productoRepository.save(producto);
        });
    }

    public void cambiarEstadoProducto(Long id, Boolean activo) {
        productoRepository.findById(id).ifPresent(producto -> {
            producto.setActivo(activo);
            productoRepository.save(producto);
            // Notificar a Inventario
            try {
                tienda.api.catalogo.event.EstadoProductoCambiadoEvent event = new tienda.api.catalogo.event.EstadoProductoCambiadoEvent(id, activo);
                rabbitTemplate.convertAndSend(tienda.api.catalogo.config.RabbitMQConfig.EXCHANGE, tienda.api.catalogo.config.RabbitMQConfig.ROUTING_KEY_ESTADO, event);
                System.out.println("Evento EstadoProductoCambiadoEvent publicado para producto: " + id);
            } catch (Exception e) {
                System.err.println("Fallo al notificar a inventario sobre cambio de estado (RabbitMQ): " + e.getMessage());
            }
        });
    }
}
