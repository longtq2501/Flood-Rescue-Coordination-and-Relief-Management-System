package com.floodrescue.notification.shared.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "rescue.events";

    // Queues
    public static final String Q_NOTIF_REQUEST_CREATED = "notif.request.created.queue";
    public static final String Q_NOTIF_REQUEST_ASSIGNED = "notif.request.assigned.queue";
    public static final String Q_NOTIF_REQUEST_COMPLETED = "notif.request.completed.queue";
    public static final String Q_NOTIF_REQUEST_STATUS = "notif.request.status.queue";
    public static final String Q_LOCATION = "notif.location.queue";
    public static final String Q_NOTIF_RESOURCE_LOW = "notif.resource.low.queue";
    public static final String Q_NOTIF_BROADCAST = "notif.broadcast.queue";

    // Routing Keys
    public static final String RK_REQUEST_CREATED = "rescue.request.created";
    public static final String RK_REQUEST_ASSIGNED = "rescue.request.assigned";
    public static final String RK_REQUEST_COMPLETED = "rescue.request.completed";
    public static final String RK_REQUEST_STATUS = "rescue.request.status.updated";
    public static final String RK_LOCATION_UPDATE = "rescue.team.location.updated";
    public static final String RK_RESOURCE_LOW = "rescue.resource.stock.low";
    public static final String RK_BROADCAST = "rescue.system.broadcast";

    @Bean
    public TopicExchange exchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Queues
    @Bean
    public Queue qNotifCreated() {
        return new Queue(Q_NOTIF_REQUEST_CREATED);
    }

    @Bean
    public Queue qNotifAssigned() {
        return new Queue(Q_NOTIF_REQUEST_ASSIGNED);
    }

    @Bean
    public Queue qNotifCompleted() {
        return new Queue(Q_NOTIF_REQUEST_COMPLETED);
    }

    @Bean
    public Queue qNotifStatus() {
        return new Queue(Q_NOTIF_REQUEST_STATUS);
    }

    @Bean
    public Queue qLocation() {
        return new Queue(Q_LOCATION);
    }

    @Bean
    public Queue qResourceLow() {
        return new Queue(Q_NOTIF_RESOURCE_LOW);
    }

    @Bean
    public Queue qBroadcast() {
        return new Queue(Q_NOTIF_BROADCAST);
    }

    // Bindings
    @Bean
    public Binding bNotifCreated(Queue qNotifCreated, TopicExchange exchange) {
        return BindingBuilder.bind(qNotifCreated).to(exchange).with(RK_REQUEST_CREATED);
    }

    @Bean
    public Binding bNotifAssigned(Queue qNotifAssigned, TopicExchange exchange) {
        return BindingBuilder.bind(qNotifAssigned).to(exchange).with(RK_REQUEST_ASSIGNED);
    }

    @Bean
    public Binding bNotifCompleted(Queue qNotifCompleted, TopicExchange exchange) {
        return BindingBuilder.bind(qNotifCompleted).to(exchange).with(RK_REQUEST_COMPLETED);
    }

    @Bean
    public Binding bNotifStatus(Queue qNotifStatus, TopicExchange exchange) {
        return BindingBuilder.bind(qNotifStatus).to(exchange).with(RK_REQUEST_STATUS);
    }

    @Bean
    public Binding bLocation(Queue qLocation, TopicExchange exchange) {
        return BindingBuilder.bind(qLocation).to(exchange).with(RK_LOCATION_UPDATE);
    }

    @Bean
    public Binding bResourceLow(Queue qResourceLow, TopicExchange exchange) {
        return BindingBuilder.bind(qResourceLow).to(exchange).with(RK_RESOURCE_LOW);
    }

    @Bean
    public Binding bBroadcast(Queue qBroadcast, TopicExchange exchange) {
        return BindingBuilder.bind(qBroadcast).to(exchange).with(RK_BROADCAST);
    }
}
