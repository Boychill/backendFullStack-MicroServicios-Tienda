package tienda.api.carrito.event.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tienda.api.carrito.config.RabbitMQConfig;
import tienda.api.carrito.event.PedidoPagadoEvent;
import tienda.api.carrito.service.CarritoService;
import java.util.Optional;
import tienda.api.carrito.model.Carrito;
import tienda.api.carrito.model.ItemCarrito;

@Component
public class PedidoPagadoConsumer {

    @Autowired
    private CarritoService carritoService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CARRITO)
    public void recibirPedidoPagado(PedidoPagadoEvent evento) {
        System.out.println("Recibido evento PedidoPagadoEvent para email: " + evento.getEmail());
        
        if (evento.getItems() == null || evento.getItems().isEmpty()) {
            // Vaciar todo el carrito
            carritoService.vaciarCarrito(evento.getEmail());
        } else {
            // Reducir cantidades específicas
            Optional<Carrito> optCarrito = carritoService.obtenerCarrito(evento.getEmail());
            if (optCarrito.isPresent()) {
                Carrito carrito = optCarrito.get();
                for (PedidoPagadoEvent.ItemComprado itemEvt : evento.getItems()) {
                    for (ItemCarrito ic : carrito.getItems()) {
                        if (ic.getProductoId().equals(itemEvt.getProductoId())) {
                            int nuevaCantidad = ic.getCantidad() - itemEvt.getCantidad();
                            if (nuevaCantidad <= 0) {
                                carritoService.eliminarItem(evento.getEmail(), ic.getProductoId());
                            } else {
                                carritoService.reducirCantidad(evento.getEmail(), ic.getProductoId(), itemEvt.getCantidad());
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
