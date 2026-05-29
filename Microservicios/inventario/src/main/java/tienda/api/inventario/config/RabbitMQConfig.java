package tienda.api.inventario.config;

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
    public static final String QUEUE_STOCK = "stock_actualizado_queue";
    public static final String QUEUE_ESTADO = "estado_producto_queue";
    public static final String ROUTING_KEY_STOCK = "stock.actualizado";
    public static final String ROUTING_KEY_ESTADO = "producto.estado";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue stockQueue() {
        return new Queue(QUEUE_STOCK, true);
    }

    @Bean
    public Queue estadoQueue() {
        return new Queue(QUEUE_ESTADO, true);
    }

    @Bean
    public Binding stockBinding() {
        return BindingBuilder.bind(stockQueue()).to(exchange()).with(ROUTING_KEY_STOCK);
    }

    @Bean
    public Queue compensacionStockQueue() {
        return new Queue("pedidos.compensacion.stock.queue", true);
    }

    @Bean
    public Binding compensacionStockBinding() {
        return BindingBuilder.bind(compensacionStockQueue()).to(exchange()).with("pedidos.compensacion.stock");
    }

    @Bean
    public Binding estadoBinding() {
        return BindingBuilder.bind(estadoQueue()).to(exchange()).with(ROUTING_KEY_ESTADO);
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


