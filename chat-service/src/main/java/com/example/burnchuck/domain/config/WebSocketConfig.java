package com.example.burnchuck.domain.config;

import com.example.burnchuck.domain.handler.AuthHandshakeInterceptor;
import com.example.burnchuck.domain.handler.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;
    private final AuthHandshakeInterceptor authHandshakeInterceptor;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ws://localhost:8080/ws-stomp 로 연결
        registry.addEndpoint("/ws-stomp")
                .setAllowedOrigins(frontendUrl)
                .addInterceptors(authHandshakeInterceptor)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 구독(수신) 경로 prefix
        registry.enableSimpleBroker("/sub");
        // 발행(송신) 경로 prefix
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
