package com.floodrescue.request.shared.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ config for request-service.
 * Only declares exchanges and queues this service PRODUCES or CONSUMES.
 *
 * PRODUCES:
 * - rescue.request.created → routing key
 * - rescue.request.status.updated → routing key
 *
 * CONSUMES:
 * - q.request.sync.assigned ← dispatch publishes rescue.request.assigned
 * - q.request.sync.completed ← dispatch publishes rescue.request.completed
 */
@Configuration
public class RabbitMQConfig {

    // ==================== EXCHANGES ====================
    public static final String EXCHANGE = "rescue.events";
    public static final String DLX_EXCHANGE = "rescue.dead-letter";

    // ==================== ROUTING KEYS ====================
    public static final String RK_REQUEST_CREATED = "rescue.request.created";
    public static final String RK_REQUEST_STATUS = "rescue.request.status.updated";
    public static final String RK_REQUEST_ASSIGNED = "rescue.request.assigned";
    public static final String RK_REQUEST_STARTED = "rescue.request.started";
    public static final String RK_REQUEST_COMPLETED = "rescue.request.completed";

    // ==================== QUEUE NAMES ====================
    public static final String Q_REQUEST_SYNC_ASSIGNED = "q.request.sync.assigned";
    public static final String Q_REQUEST_SYNC_STARTED = "q.request.sync.started";
    public static final String Q_REQUEST_SYNC_COMPLETED = "q.request.sync.completed";
    public static final String Q_DLQ = "q.dlq.all";

    // ==================== EXCHANGES ====================
    @Bean
    public TopicExchange rescueExchange() {
        return ExchangeBuilder
                .topicExchange(EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    // ==================== QUEUES ====================
    private Queue durableQueue(String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", Q_DLQ)
                .build();
    }

    @Bean
    public Queue qRequestSyncAssigned() {
        return durableQueue(Q_REQUEST_SYNC_ASSIGNED);
    }

    @Bean
    public Queue qRequestSyncStarted() {
        return durableQueue(Q_REQUEST_SYNC_STARTED);
    }

    @Bean
    public Queue qRequestSyncCompleted() {
        return durableQueue(Q_REQUEST_SYNC_COMPLETED);
    }

    @Bean
    public Queue qDlq() {
        return QueueBuilder.durable(Q_DLQ).build();
    }

    // ==================== BINDINGS ====================
    @Bean
    public Binding bindRequestSyncAssigned() {
        return BindingBuilder.bind(qRequestSyncAssigned())
                .to(rescueExchange()).with(RK_REQUEST_ASSIGNED);
    }

    @Bean
    public Binding bindRequestSyncStarted() {
        return BindingBuilder.bind(qRequestSyncStarted())
                .to(rescueExchange()).with(RK_REQUEST_STARTED);
    }

    @Bean
    public Binding bindRequestSyncCompleted() {
        return BindingBuilder.bind(qRequestSyncCompleted())
                .to(rescueExchange()).with(RK_REQUEST_COMPLETED);
    }

    @Bean
    public Binding bindDlq() {
        return BindingBuilder.bind(qDlq())
                .to(deadLetterExchange()).with(Q_DLQ);
    }

    // ==================== MESSAGE CONVERTER ====================
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ==================== RABBIT TEMPLATE ====================
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        return template;
    }
}
