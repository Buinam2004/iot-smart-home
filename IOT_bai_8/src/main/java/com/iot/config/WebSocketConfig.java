package com.iot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // WebSocket endpoint ( nơi client kết nối đến)
                .setAllowedOriginPatterns("*") // cho phép tất cả các nguồn gốc (CORS)
                .withSockJS(); // fallback for old browsers
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // client → server
        registry.setApplicationDestinationPrefixes("/app"); // tiền tố cho các tin nhắn gửi từ client đến server

        // server → client
        registry.enableSimpleBroker("/topic", "/queue"); // tiền tố cho các tin nhắn gửi từ server đến client
    }
}
