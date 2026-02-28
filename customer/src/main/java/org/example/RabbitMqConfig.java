package org.example;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration with SAGA pattern support.
 * Uses topic exchange for SAGA event routing.
 */
@Configuration
public class RabbitMqConfig {

    // Existing queues
    public static final String ORDER_QUEUE = "order_queue";
    public static final String DRONE_QUEUE = "drone_queue";

    // SAGA-specific exchange and queues
    public static final String SAGA_EVENTS_EXCHANGE = "saga_events_exchange";
    public static final String SAGA_EVENTS_QUEUE = "saga_events_queue";
    public static final String SAGA_COMPENSATION_QUEUE = "saga_compensation_queue";

    @Value("${spring.rabbitmq.host:localhost}")
    private String rabbitmqHost;

    @Value("${spring.rabbitmq.username:guest}")
    private String rabbitmqUsername;

    @Value("${spring.rabbitmq.password:guest}")
    private String rabbitmqPassword;

    // Existing queues
    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true);
    }

    @Bean
    public Queue droneQueue() {
        return new Queue(DRONE_QUEUE, true);
    }

    // SAGA queues
    @Bean
    public Queue sagaEventsQueue() {
        return new Queue(SAGA_EVENTS_QUEUE, true);
    }

    @Bean
    public Queue sagaCompensationQueue() {
        return new Queue(SAGA_COMPENSATION_QUEUE, true);
    }

    // Topic exchange for SAGA events
    @Bean
    public TopicExchange sagaEventsExchange() {
        return new TopicExchange(SAGA_EVENTS_EXCHANGE);
    }

    // Bindings for SAGA events
    @Bean
    public Binding sagaEventsBinding(Queue sagaEventsQueue, TopicExchange sagaEventsExchange) {
        return BindingBuilder.bind(sagaEventsQueue)
                .to(sagaEventsExchange)
                .with("saga.order.*");
    }

    // Bindings for compensation events
    @Bean
    public Binding sagaCompensationBinding(Queue sagaCompensationQueue,
                                           TopicExchange sagaEventsExchange) {
        return BindingBuilder.bind(sagaCompensationQueue)
                .to(sagaEventsExchange)
                .with("saga.compensate_order");
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(rabbitmqHost);
        factory.setUsername(rabbitmqUsername);
        factory.setPassword(rabbitmqPassword);
        return factory;
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}