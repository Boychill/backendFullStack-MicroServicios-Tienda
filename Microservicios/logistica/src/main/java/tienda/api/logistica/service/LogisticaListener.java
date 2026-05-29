package tienda.api.logistica.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tienda.api.logistica.config.RabbitMQConfig;

import java.util.Map;

@Component
public class LogisticaListener {

    @Autowired
    private LogisticaService logisticaService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_LOGISTICA)
    public void handlePedidoDespachoEvent(Map<String, Object> event) {
        try {
            if (event.containsKey("pedidoId")) {
                Long pedidoId = Long.parseLong(event.get("pedidoId").toString());
                String direccionCompleta = event.containsKey("direccionCompleta") ? event.get("direccionCompleta").toString() : "Direccion no especificada";
                logisticaService.crearGuiaParaPedido(pedidoId, direccionCompleta);
                System.out.println("====== [LOGISTICA] Guia de despacho creada para el Pedido ID: " + pedidoId + " ======");
            }
        } catch (Exception e) {
            System.err.println("Error procesando evento de despacho: " + e.getMessage());
        }
    }
}
