package tienda.api.notificaciones.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "tienda_exchange";
    public static final String QUEUE_NOTIFICACIONES = "notificaciones_queue";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue notificacionesQueue() {
        return new Queue(QUEUE_NOTIFICACIONES, true);
    }

    @Bean
    public Binding bindingPagado() {
        return BindingBuilder.bind(notificacionesQueue()).to(exchange()).with("carrito.vaciar"); // Using the same routing key as PedidoPagadoEvent
    }

    @Bean
    public Binding bindingFallido() {
        return BindingBuilder.bind(notificacionesQueue()).to(exchange()).with("pedidos.fallido");
    }

    @Bean
    public Binding bindingDevolucion() {
        return BindingBuilder.bind(notificacionesQueue()).to(exchange()).with("pedidos.devolucion");
    }

    @Bean
    public Binding bindingLogisticaEstado() {
        return BindingBuilder.bind(notificacionesQueue()).to(exchange()).with("logistica.estado.cambiado");
    }

    @Bean
    public Binding bindingLogisticaAsignado() {
        return BindingBuilder.bind(notificacionesQueue()).to(exchange()).with("logistica.ruta.asignada");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
