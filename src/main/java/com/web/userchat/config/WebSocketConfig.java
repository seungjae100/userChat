package com.web.userchat.config;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.CloseStatus;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(), "/chat") // 클라이언트가 연결할 엔드포인트 설정
                .addInterceptors(new HttpSessionHandshakeInterceptor()) // 핸드셰이크 요청 시 세션 정보를 사용할 수 있도록 인터셉터 추가
                .setAllowedOrigins("*"); // 전체 도메인 설정
    }

    private static class ChatWebSocketHandler extends AbstractWebSocketHandler {
        // 채팅방 ID별로 세션을 관리하기 위한 맵
        private static final Map<String, Set<WebSocketSession>> chatRooms = new ConcurrentHashMap<>();

        @Override
        public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
            String receivedMessage = message.getPayload();
            System.out.println("Received: " + receivedMessage);

            // 메시지 파싱 (JSON 형태라고 가정)
            try {
                JSONObject jsonMessage = new JSONObject(receivedMessage);
                String chatRoomId = jsonMessage.getString("chattingRoomId");
                String chatMessage = jsonMessage.getString("content");

                // 채팅방에 세션 등록
                chatRooms.computeIfAbsent(chatRoomId, k -> new CopyOnWriteArraySet<>()).add(session);

                // 해당 채팅방의 모든 사용자에게 메시지 브로드캐스트
                for (WebSocketSession webSocketSession : chatRooms.get(chatRoomId)) {
                    if (webSocketSession.isOpen()) {
                        webSocketSession.sendMessage(new TextMessage(jsonMessage.toString()));
                    }
                }
            } catch (JSONException e) {
                session.sendMessage(new TextMessage("{\"error\": \"Invalid chatRoomId\"}"));
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
            // 세션이 닫힐 때 해당 세션을 모든 채팅방에서 제거
            chatRooms.values().forEach(sessions -> sessions.remove(session));
            System.out.println("WebSocket session closed: " + session.getId() + ", status: " + status);
        }
    }
}
