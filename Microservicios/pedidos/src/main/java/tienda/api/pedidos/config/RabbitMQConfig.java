package tienda.api.pedidos.config;

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
    public static final String QUEUE_CARRITO = "vaciar_carrito_queue";
    public static final String ROUTING_KEY_CARRITO = "carrito.vaciar";
    public static final String ROUTING_KEY_LOGISTICA = "logistica.despachar";

    public static final String QUEUE_ESTADOS_PEDIDO = "pedidos_estado_queue";
    public static final String ROUTING_KEY_LOGISTICA_ESTADO = "logistica.estado.cambiado";
    public static final String ROUTING_KEY_PAGOS_REEMBOLSO = "pagos.reembolso.exitoso";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue carritoQueue() {
        return new Queue(QUEUE_CARRITO, true);
    }

    @Bean
    public Queue estadosPedidoQueue() {
        return new Queue(QUEUE_ESTADOS_PEDIDO, true);
    }

    @Bean
    public Binding carritoBinding() {
        return BindingBuilder.bind(carritoQueue()).to(exchange()).with(ROUTING_KEY_CARRITO);
    }

    @Bean
    public Binding logisticaEstadoBinding() {
        return BindingBuilder.bind(estadosPedidoQueue()).to(exchange()).with(ROUTING_KEY_LOGISTICA_ESTADO);
    }

    @Bean
    public Binding pagosReembolsoBinding() {
        return BindingBuilder.bind(estadosPedidoQueue()).to(exchange()).with(ROUTING_KEY_PAGOS_REEMBOLSO);
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
