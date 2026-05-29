package tienda.api.logistica.config;

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
    public static final String QUEUE_LOGISTICA = "crear_despacho_queue";
    public static final String ROUTING_KEY_LOGISTICA = "logistica.despachar";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue logisticaQueue() {
        return new Queue(QUEUE_LOGISTICA, true);
    }

    @Bean
    public Binding logisticaBinding() {
        return BindingBuilder.bind(logisticaQueue()).to(exchange()).with(ROUTING_KEY_LOGISTICA);
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
