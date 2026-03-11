package com.floodrescue.report.shared.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho các topic server → client
        registry.enableSimpleBroker(
            "/topic",   // broadcast (map tracking, dispatch board)
            "/queue"    // private per-user (team task queue)
        );

        // Prefix cho các message client → server
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix cho private queue per user
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // production: đổi thành domain cụ thể
                .withSockJS();                  // fallback cho browser không hỗ trợ WS native
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Inject auth interceptor để validate JWT khi client CONNECT
        registration.interceptors(webSocketAuthInterceptor);
    }
}
