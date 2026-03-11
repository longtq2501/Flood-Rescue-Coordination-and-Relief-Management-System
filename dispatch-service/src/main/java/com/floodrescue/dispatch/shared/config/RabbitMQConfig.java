package com.floodrescue.dispatch.shared.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "rescue.events";
    
    // Routing Keys (Produced)
    public static final String RK_REQUEST_ASSIGNED = "rescue.request.assigned";
    public static final String RK_REQUEST_COMPLETED = "rescue.request.completed";
    public static final String RK_TEAM_LOCATION = "rescue.team.location";

    // Routing Keys (Consumed)
    public static final String RK_LOCATION = "location.update";

    // Queues
    public static final String Q_LOCATION = "q.dispatch.location";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue locationQueue() {
        return QueueBuilder.durable(Q_LOCATION)
                .withArgument("x-dead-letter-exchange", EXCHANGE + ".dlx")
                .withArgument("x-dead-letter-routing-key", Q_LOCATION + ".dlq")
                .build();
    }

    @Bean
    public Binding locationBinding(Queue locationQueue, DirectExchange exchange) {
        return BindingBuilder.bind(locationQueue).to(exchange).with(RK_LOCATION);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
