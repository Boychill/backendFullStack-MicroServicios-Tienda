package tienda.api.pedidos.event;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tienda.api.pedidos.model.Pedido;
import tienda.api.pedidos.repository.PedidoRepository;
import tienda.api.pedidos.config.RabbitMQConfig;

import java.util.Map;

@Component
public class PedidoEventListener {

    @Autowired
    private PedidoRepository pedidoRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ESTADOS_PEDIDO)
    public void handleEstadoCambio(Map<String, Object> message) {
        try {
            Long pedidoId = Long.parseLong(message.get("pedidoId").toString());
            
            pedidoRepository.findById(pedidoId).ifPresent(pedido -> {
                // Si el mensaje viene de Logística, tendrá un "estado"
                if (message.containsKey("estado")) {
                    String estadoLogistica = message.get("estado").toString();
                    
                    // Traducción simple del estado (puede ser más compleja según la lógica de negocio)
                    if ("ENTREGADO".equals(estadoLogistica)) {
                        pedido.setEstado("ENTREGADO");
                    } else if ("NO_RESPUESTA".equals(estadoLogistica) || "DIRECCION_ERRONEA".equals(estadoLogistica)) {
                        pedido.setEstado("FALLIDO_ENTREGA");
                    } else if ("EN_RUTA".equals(estadoLogistica)) {
                        pedido.setEstado("EN_ENTREGA");
                    }
                }
                
                // Si el mensaje viene de Pagos, tendrá un "refundId"
                if (message.containsKey("refundId")) {
                    pedido.setEstado("REEMBOLSADO");
                }
                
                pedidoRepository.save(pedido);
                System.out.println("Estado del pedido " + pedidoId + " actualizado a: " + pedido.getEstado());
            });
        } catch (Exception e) {
            System.err.println("Error procesando actualización de estado del pedido: " + e.getMessage());
        }
    }
}
