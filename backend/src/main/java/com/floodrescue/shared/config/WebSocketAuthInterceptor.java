package com.floodrescue.shared.config;

import com.floodrescue.shared.security.JwtTokenProvider;
import com.floodrescue.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * WebSocket auth interceptor that validates JWT directly.
 * WebSocket connections bypass the Gateway's header forwarding,
 * so this interceptor validates the JWT token itself.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                log.warn("WebSocket CONNECT rejected: missing token");
                throw new IllegalArgumentException("Missing Authorization header");
            }

            String token = authHeader.substring(7);

            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("WebSocket CONNECT rejected: invalid token");
                throw new IllegalArgumentException("Invalid JWT token");
            }

            Long userId  = jwtTokenProvider.getUserIdFromToken(token);
            String role  = jwtTokenProvider.getRoleFromToken(token);
            String phone = jwtTokenProvider.getPhoneFromToken(token);

            UserPrincipal principal = new UserPrincipal(userId, phone, role);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal, null, principal.getAuthorities());

            accessor.setUser(authentication);
            log.info("WebSocket CONNECT authenticated: userId={}, role={}", userId, role);
        }

        return message;
    }
}