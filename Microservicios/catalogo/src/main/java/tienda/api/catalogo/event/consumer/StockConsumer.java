package tienda.api.catalogo.event.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tienda.api.catalogo.config.RabbitMQConfig;
import tienda.api.catalogo.event.StockActualizadoEvent;
import tienda.api.catalogo.service.ProductoService;

@Component
public class StockConsumer {

    @Autowired
    private ProductoService productoService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_STOCK)
    public void recibirActualizacionStock(StockActualizadoEvent evento) {
        System.out.println("Recibido evento StockActualizadoEvent: " + evento);
        productoService.actualizarStock(evento.getProductoId(), evento.getStock());
    }
}
