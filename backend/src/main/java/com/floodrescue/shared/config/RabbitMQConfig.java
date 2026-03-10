package com.floodrescue.shared.config;

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
    public static final String RK_REQUEST_CREATED = "rescue.request.created";
    public static final String RK_REQUEST_ASSIGNED = "rescue.request.assigned";
    public static final String RK_REQUEST_COMPLETED = "rescue.request.completed";
    public static final String RK_REQUEST_STATUS = "rescue.request.status.updated";
    public static final String RK_RESOURCE_LOW = "rescue.resource.stock.low";
    public static final String RK_RESOURCE_DIST = "rescue.resource.distributed";
    public static final String RK_TEAM_LOCATION = "rescue.team.location.updated";
    public static final String RK_BROADCAST = "rescue.system.broadcast";

    // ==================== QUEUE NAMES ====================
    public static final String Q_NOTIF_REQUEST_CREATED = "q.notification.request.created";
    public static final String Q_NOTIF_REQUEST_ASSIGNED = "q.notification.request.assigned";
    public static final String Q_NOTIF_REQUEST_COMPLETED = "q.notification.request.completed";
    public static final String Q_NOTIF_REQUEST_STATUS = "q.notification.request.status";
    public static final String Q_NOTIF_RESOURCE_LOW = "q.notification.resource.low";
    public static final String Q_NOTIF_BROADCAST = "q.notification.system.broadcast";
    public static final String Q_REPORT_COMPLETED = "q.report.request.completed";
    public static final String Q_REPORT_DISTRIBUTED = "q.report.resource.distributed";
    public static final String Q_LOCATION = "q.dispatch.location.updated";
    public static final String Q_REQUEST_SYNC_ASSIGNED = "q.request.sync.assigned";
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
    // Helper tạo queue durable + gắn dead-letter exchange
    private Queue durableQueue(String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", Q_DLQ)
                .build();
    }

    @Bean
    public Queue qNotifRequestCreated() {
        return durableQueue(Q_NOTIF_REQUEST_CREATED);
    }

    @Bean
    public Queue qNotifRequestAssigned() {
        return durableQueue(Q_NOTIF_REQUEST_ASSIGNED);
    }

    @Bean
    public Queue qNotifRequestCompleted() {
        return durableQueue(Q_NOTIF_REQUEST_COMPLETED);
    }

    @Bean
    public Queue qNotifRequestStatus() {
        return durableQueue(Q_NOTIF_REQUEST_STATUS);
    }

    @Bean
    public Queue qNotifResourceLow() {
        return durableQueue(Q_NOTIF_RESOURCE_LOW);
    }

    @Bean
    public Queue qNotifBroadcast() {
        return durableQueue(Q_NOTIF_BROADCAST);
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
    public Queue qLocation() {
        return durableQueue(Q_LOCATION);
    }

    @Bean
    public Queue qRequestSyncAssigned() {
        return durableQueue(Q_REQUEST_SYNC_ASSIGNED);
    }

    @Bean
    public Queue qRequestSyncCompleted() {
        return durableQueue(Q_REQUEST_SYNC_COMPLETED);
    }

    @Bean
    public Queue qDlq() {
        // DLQ không cần dead-letter nữa
        return QueueBuilder.durable(Q_DLQ).build();
    }

    // ==================== BINDINGS ====================
    @Bean
    public Binding bindNotifRequestCreated() {
        return BindingBuilder.bind(qNotifRequestCreated())
                .to(rescueExchange()).with(RK_REQUEST_CREATED);
    }

    @Bean
    public Binding bindNotifRequestAssigned() {
        return BindingBuilder.bind(qNotifRequestAssigned())
                .to(rescueExchange()).with(RK_REQUEST_ASSIGNED);
    }

    @Bean
    public Binding bindNotifRequestCompleted() {
        return BindingBuilder.bind(qNotifRequestCompleted())
                .to(rescueExchange()).with(RK_REQUEST_COMPLETED);
    }

    @Bean
    public Binding bindNotifRequestStatus() {
        return BindingBuilder.bind(qNotifRequestStatus())
                .to(rescueExchange()).with(RK_REQUEST_STATUS);
    }

    @Bean
    public Binding bindNotifResourceLow() {
        return BindingBuilder.bind(qNotifResourceLow())
                .to(rescueExchange()).with(RK_RESOURCE_LOW);
    }

    @Bean
    public Binding bindNotifBroadcast() {
        return BindingBuilder.bind(qNotifBroadcast())
                .to(rescueExchange()).with(RK_BROADCAST);
    }

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
    public Binding bindLocation() {
        return BindingBuilder.bind(qLocation())
                .to(rescueExchange()).with(RK_TEAM_LOCATION);
    }

    @Bean
    public Binding bindDlq() {
        return BindingBuilder.bind(qDlq())
                .to(deadLetterExchange()).with(Q_DLQ);
    }

    @Bean
    public Binding bindRequestSyncAssigned() {
        return BindingBuilder.bind(qRequestSyncAssigned())
                .to(rescueExchange()).with(RK_REQUEST_ASSIGNED);
    }

    @Bean
    public Binding bindRequestSyncCompleted() {
        return BindingBuilder.bind(qRequestSyncCompleted())
                .to(rescueExchange()).with(RK_REQUEST_COMPLETED);
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
        // Bật confirm callback để biết message có tới exchange không
        template.setMandatory(true);
        return template;
    }
}