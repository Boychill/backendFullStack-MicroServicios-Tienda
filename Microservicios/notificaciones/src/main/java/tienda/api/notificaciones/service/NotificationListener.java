package tienda.api.notificaciones.service;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tienda.api.notificaciones.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class NotificationListener {

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICACIONES)
    public void processNotification(Message message) {
        try {
            String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            byte[] body = message.getBody();
            Map<String, Object> event = objectMapper.readValue(body, Map.class);
            
            Long usuarioId = event.containsKey("usuarioId") ? Long.parseLong(String.valueOf(event.get("usuarioId"))) : null; String email = event.containsKey("email") ? (String) event.get("email") : "";
            Long pedidoId = event.containsKey("pedidoId") ? Long.parseLong(event.get("pedidoId").toString()) : null;

            if (routingKey.equals("carrito.vaciar")) {
                // This is actually PedidoPagadoEvent
                notificacionService.crearNotificacion(usuarioId, "ROLE_USER", "COMPRA_CONFIRMADA", "Tu pedido #" + pedidoId + " ha sido pagado y confirmado con éxito.");
                notificacionService.crearNotificacion(-1L, "ROLE_ADMIN", "NUEVA_VENTA", "Se ha registrado una nueva venta pagada para el pedido #" + pedidoId + " por " + email);
            
            } else if (routingKey.equals("pedidos.fallido")) {
                String motivo = (String) event.get("motivo");
                notificacionService.crearNotificacion(usuarioId, "ROLE_USER", "COMPRA_FALLIDA", "Tu intento de compra para el pedido #" + pedidoId + " ha fallado. Motivo: " + motivo);
                notificacionService.crearNotificacion(-1L, "ROLE_ADMIN", "ALERTA_FALLO", "Falló el checkout del pedido #" + pedidoId + " de " + usuarioId + ". Motivo: " + motivo);
            
            } else if (routingKey.equals("pedidos.devolucion")) {
                notificacionService.crearNotificacion(usuarioId, "ROLE_USER", "DEVOLUCION_EXITOSA", "La devolución del pedido #" + pedidoId + " ha sido procesada correctamente.");
                notificacionService.crearNotificacion(-1L, "ROLE_ADMIN", "DEVOLUCION", "El cliente " + usuarioId + " ha devuelto el pedido #" + pedidoId + ".");
            
            } else if (routingKey.equals("logistica.estado.cambiado")) {
                String estado = (String) event.get("estado");
                Long choferId = Long.parseLong(String.valueOf(event.get("choferId")));
                
                Long clienteId = -1L; // Fallback
                try {
                    var pedidos = tienda.api.notificaciones.NotificacionesApplication.context.getBean(tienda.api.notificaciones.client.PedidoClient.class).obtenerTodosLosPedidos();
                    for(var p : pedidos) {
                        if (p.getId().equals(pedidoId)) clienteId = p.getUsuarioId();
                    }
                } catch(Exception ignored) {}

                if (estado.equals("NO_RESPUESTA")) {
                    notificacionService.crearNotificacion(-1L, "ROLE_ADMIN", "ALERTA_LOGISTICA", "El chofer " + choferId + " reportó que no hubo respuesta al entregar el pedido #" + pedidoId);
                    notificacionService.crearNotificacion(clienteId, "ROLE_USER", "INTENTO_ENTREGA_FALLIDO", "Intento de entrega fallido para el pedido #" + pedidoId + ". El chofer indicó que no hubo respuesta en el domicilio.");
                } else if (estado.equals("ENTREGADO")) {
                    notificacionService.crearNotificacion(-1L, "ROLE_ADMIN", "ENTREGA_COMPLETADA", "El pedido #" + pedidoId + " fue entregado con éxito por " + choferId);
                    notificacionService.crearNotificacion(clienteId, "ROLE_USER", "PEDIDO_ENTREGADO", "¡Tu pedido #" + pedidoId + " ha sido entregado exitosamente!");
                } else if (estado.equals("EN_RUTA")) {
                    notificacionService.crearNotificacion(clienteId, "ROLE_USER", "PEDIDO_EN_RUTA", "Tu pedido #" + pedidoId + " ya va en camino hacia tu domicilio.");
                } else if (estado.equals("CANCELADA")) {
                    if (choferId != -1L) {
                        notificacionService.crearNotificacion(choferId, "ROLE_CHOFER", "RUTA_CANCELADA", "La ruta para el pedido #" + pedidoId + " ha sido cancelada.");
                    }
                    notificacionService.crearNotificacion(clienteId, "ROLE_USER", "PEDIDO_CANCELADO", "Lo sentimos, el envío de tu pedido #" + pedidoId + " ha sido cancelado.");
                } else if (estado.equals("LISTO_PARA_CHOFER")) {
                    notificacionService.crearNotificacion(-1L, "ROLE_ADMIN", "LOGISTICA_LISTA", "El pedido #" + pedidoId + " ya está armado y listo para asignarse a un chofer.");
                }
            
            } else if (routingKey.equals("logistica.ruta.asignada")) {
                Long choferId = Long.parseLong(String.valueOf(event.get("choferId")));
                String estado = (String) event.get("estado");
                if (estado.equals("ASIGNADO_REASIGNACION")) {
                    notificacionService.crearNotificacion(choferId, "ROLE_CHOFER", "NUEVA_RUTA", "Se te ha reasignado urgentemente la ruta para entregar el pedido #" + pedidoId);
                } else {
                    notificacionService.crearNotificacion(choferId, "ROLE_CHOFER", "NUEVA_RUTA", "Se te ha asignado una nueva ruta para el pedido #" + pedidoId);
                }
            }

        } catch (Exception e) {
            System.err.println("Error procesando notificacion: " + e.getMessage());
        }
    }
}
