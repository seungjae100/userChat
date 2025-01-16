package com.web.userchat.config;

import com.web.userchat.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 메시지를 구독하는 목적지에 대한 프리픽스를 설정
        config.enableSimpleBroker("/topic");
        // 클라이언트가 메시지를 보낼 때 사용할 엔드포인트 프리픽스를 설정
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 웹소켓 서버에 연결할 수 있는 엔드포인트를 설정
        registry.addEndpoint("/chat")
                .setAllowedOriginPatterns("*") // CORS 설정
                .withSockJS(); // SockJS를 사용하여 웹소켓이 지원되지 않는 경우 대체하기 위해 설정
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                // STOMP 헤더 접근을 위한 accessor 생성
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                // CONNECT 커맨드일 경우에만 인증 처리
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // 쿠키에서 JWT 토큰 추출
                    List<String> cookies = accessor.getNativeHeader("Set-Cookie");
                    if (cookies != null && !cookies.isEmpty()) {
                        String accessToken = extractAccessToken(cookies.get(0));
                        if (accessToken != null && jwtService.validateToken(accessToken)) {
                            // 토큰에서 사용자 정보 추출하여 WebSocket 연결에 설정
                            String email = jwtService.getUserEmailFromToken(accessToken);
                            accessor.setUser(new Principal() {
                                @Override
                                public String getName() {
                                    return email;
                                }
                            });
                        }
                    }
                }
                return message;
            }
        });
    }

    private String extractAccessToken(String cookieHeader) {
        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            if (cookie.trim().startsWith("accessToken=")) {
                return cookie.trim().substring("accessToken=".length());
            }
        }
        return null;
    }
}
