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

    public List<Producto> obtenerTodos(String categoria) {
        if(categoria != null && !categoria.isEmpty()){
            return productoRepository.findByCategoriaAndActivoTrue(categoria);
        }
        return productoRepository.findByActivoTrue();
    }

    public Optional<Producto> obenterPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto guardar(Producto producto) {
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
        });
    }
}
