package tienda.api.carrito.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.api.carrito.client.CatalogoClient;
import tienda.api.carrito.model.Carrito;
import tienda.api.carrito.model.CartItem;
import tienda.api.carrito.repository.CarritoRepository;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private CatalogoClient catalogoClient;

    public Carrito obtenerCarrito(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId).orElseGet(() -> {
            Carrito nuevo = new Carrito();
            nuevo.setUsuarioId(usuarioId);
            return carritoRepository.save(nuevo);
        });
    }

    @Transactional
    public Carrito agregarItem(Long usuarioId, Long productoId, Integer cantidad) {
        java.util.Map<String, Object> producto = catalogoClient.obtenerProducto(productoId);
        if (producto == null || !producto.containsKey("precio")) {
            throw new RuntimeException("Producto no encontrado o precio invalido");
        }
        
        Object activoObj = producto.get("activo");
        if (activoObj != null && !Boolean.parseBoolean(activoObj.toString())) {
            throw new RuntimeException("El producto seleccionado no esta activo y no puede ser comprado.");
        }

        BigDecimal precioReal = new BigDecimal(producto.get("precio").toString());

        Carrito carrito = obtenerCarrito(usuarioId);
        
        Optional<CartItem> itemOpt = carrito.getItems().stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst();
                
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            item.setCantidad(item.getCantidad() + cantidad);
            item.setPrecioUnitario(precioReal);
            item.setSubtotal(precioReal.multiply(new BigDecimal(item.getCantidad())));
        } else {
            CartItem nuevoItem = new CartItem();
            nuevoItem.setCarritoId(carrito.getId());
            nuevoItem.setProductoId(productoId);
            nuevoItem.setCantidad(cantidad);
            nuevoItem.setPrecioUnitario(precioReal);
            nuevoItem.setSubtotal(precioReal.multiply(new BigDecimal(cantidad)));
            carrito.getItems().add(nuevoItem);
        }
        
        recalcularTotal(carrito);
        return carritoRepository.save(carrito);
    }
    
    @Transactional
    public void vaciarCarrito(Long usuarioId) {
        Carrito carrito = obtenerCarrito(usuarioId);
        carrito.getItems().clear();
        carrito.setTotal(BigDecimal.ZERO);
        carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito reducirCantidadItem(Long usuarioId, Long productoId, Integer cantidadAQuitar) {
        Carrito carrito = obtenerCarrito(usuarioId);
        
        Optional<CartItem> itemOpt = carrito.getItems().stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst();
                
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            int nuevaCantidad = item.getCantidad() - cantidadAQuitar;
            
            if (nuevaCantidad <= 0) {
                carrito.getItems().remove(item);
            } else {
                item.setCantidad(nuevaCantidad);
                item.setSubtotal(item.getPrecioUnitario().multiply(new BigDecimal(nuevaCantidad)));
            }
            recalcularTotal(carrito);
            return carritoRepository.save(carrito);
        }
        return carrito;
    }

    @Transactional
    public Carrito eliminarItem(Long usuarioId, Long productoId) {
        Carrito carrito = obtenerCarrito(usuarioId);
        
        boolean eliminado = carrito.getItems().removeIf(i -> i.getProductoId().equals(productoId));
        if (eliminado) {
            recalcularTotal(carrito);
            return carritoRepository.save(carrito);
        }
        return carrito;
    }

    private void recalcularTotal(Carrito carrito) {
        BigDecimal total = carrito.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        carrito.setTotal(total);
    }
}

