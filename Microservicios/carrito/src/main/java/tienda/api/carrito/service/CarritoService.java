package tienda.api.carrito.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.api.carrito.model.Carrito;
import tienda.api.carrito.model.CartItem;
import tienda.api.carrito.repository.CarritoRepository;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    public Carrito obtenerCarrito(String email) {
        return carritoRepository.findByUsuarioEmail(email).orElseGet(() -> {
            Carrito nuevo = new Carrito();
            nuevo.setUsuarioEmail(email);
            return carritoRepository.save(nuevo);
        });
    }

    @Transactional
    public Carrito agregarItem(String email, Long productoId, Integer cantidad, BigDecimal precio) {
        Carrito carrito = obtenerCarrito(email);
        
        Optional<CartItem> itemOpt = carrito.getItems().stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst();
                
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            item.setCantidad(item.getCantidad() + cantidad);
            item.setSubtotal(item.getPrecioUnitario().multiply(new BigDecimal(item.getCantidad())));
        } else {
            CartItem nuevoItem = new CartItem();
            nuevoItem.setCarritoId(carrito.getId());
            nuevoItem.setProductoId(productoId);
            nuevoItem.setCantidad(cantidad);
            nuevoItem.setPrecioUnitario(precio);
            nuevoItem.setSubtotal(precio.multiply(new BigDecimal(cantidad)));
            carrito.getItems().add(nuevoItem);
        }
        
        recalcularTotal(carrito);
        return carritoRepository.save(carrito);
    }
    
    @Transactional
    public void vaciarCarrito(String email) {
        Carrito carrito = obtenerCarrito(email);
        carrito.getItems().clear();
        carrito.setTotal(BigDecimal.ZERO);
        carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito reducirCantidadItem(String email, Long productoId, Integer cantidadAQuitar) {
        Carrito carrito = obtenerCarrito(email);
        
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
    public Carrito eliminarItem(String email, Long productoId) {
        Carrito carrito = obtenerCarrito(email);
        
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
