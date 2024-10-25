package com.web.userchat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 클라이언트에 메세지를 전달하기 위한 브로커 설정
        config.setApplicationDestinationPrefixes("/app"); // 클라이언트가 메세지를 보낼 때 사용할 prefix
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat-websocket") // 클라이언트가 연결한 엔드포인트 설정
                .setAllowedOrigins("http://localhost:8080")  // 특정 도메인에서 접근 가능하도록 설정
                .withSockJS(); // SockJS 지원
    }

}
