package com.floodrescue.report.shared.config;

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

@Configuration
public class RabbitMQConfig {

    // ==================== EXCHANGES ====================
    public static final String EXCHANGE = "rescue.events";
    public static final String DLX_EXCHANGE = "rescue.dead-letter";

    // ==================== ROUTING KEYS ====================
    public static final String RK_REQUEST_COMPLETED = "rescue.request.completed";
    public static final String RK_RESOURCE_DIST = "rescue.resource.distributed";

    // ==================== QUEUE NAMES ====================
    public static final String Q_REPORT_COMPLETED = "q.report.request.completed";
    public static final String Q_REPORT_DISTRIBUTED = "q.report.resource.distributed";
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
    public Queue qReportCompleted() {
        return durableQueue(Q_REPORT_COMPLETED);
    }

    @Bean
    public Queue qReportDistributed() {
        return durableQueue(Q_REPORT_DISTRIBUTED);
    }

    @Bean
    public Queue qDlq() {
        return QueueBuilder.durable(Q_DLQ).build();
    }

    // ==================== BINDINGS ====================
    @Bean
    public Binding bindReportCompleted() {
        return BindingBuilder.bind(qReportCompleted())
                .to(rescueExchange()).with(RK_REQUEST_COMPLETED);
    }

    @Bean
    public Binding bindReportDistributed() {
        return BindingBuilder.bind(qReportDistributed())
                .to(rescueExchange()).with(RK_RESOURCE_DIST);
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