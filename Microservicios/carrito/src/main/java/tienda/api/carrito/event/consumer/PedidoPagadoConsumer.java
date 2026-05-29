package tienda.api.carrito.event.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tienda.api.carrito.config.RabbitMQConfig;
import tienda.api.carrito.event.PedidoPagadoEvent;
import tienda.api.carrito.service.CarritoService;
import java.util.Optional;
import tienda.api.carrito.model.Carrito;
import tienda.api.carrito.model.CartItem;

@Component
public class PedidoPagadoConsumer {

    @Autowired
    private CarritoService carritoService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CARRITO)
    public void recibirPedidoPagado(PedidoPagadoEvent evento) {
        System.out.println("Recibido evento PedidoPagadoEvent para email: " + evento.getUsuarioId());
        
        if (evento.getItems() == null || evento.getItems().isEmpty()) {
            // Vaciar todo el carrito
            carritoService.vaciarCarrito(evento.getUsuarioId());
        } else {
            // Reducir cantidades específicas
            Carrito carrito = carritoService.obtenerCarrito(evento.getUsuarioId());
            for (PedidoPagadoEvent.ItemComprado itemEvt : evento.getItems()) {
                for (CartItem ic : carrito.getItems()) {
                    if (ic.getProductoId().equals(itemEvt.getProductoId())) {
                        int nuevaCantidad = ic.getCantidad() - itemEvt.getCantidad();
                        if (nuevaCantidad <= 0) {
                            carritoService.eliminarItem(evento.getUsuarioId(), ic.getProductoId());
                        } else {
                            carritoService.reducirCantidadItem(evento.getUsuarioId(), ic.getProductoId(), itemEvt.getCantidad());
                        }
                        break;
                    }
                }
            }
        }
    }
}
