package tienda.api.inventario.event.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tienda.api.inventario.config.RabbitMQConfig;
import tienda.api.inventario.event.EstadoProductoCambiadoEvent;
import tienda.api.inventario.service.InventarioService;

@Component
public class EstadoProductoConsumer {

    @Autowired
    private InventarioService inventarioService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ESTADO)
    public void recibirCambioEstado(EstadoProductoCambiadoEvent evento) {
        System.out.println("Recibido evento EstadoProductoCambiadoEvent: " + evento);
        inventarioService.desactivarProducto(evento.getProductoId(), evento.getActivo());
    }
}
