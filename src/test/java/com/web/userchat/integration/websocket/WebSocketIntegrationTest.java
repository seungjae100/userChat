package com.web.userchat.integration.websocket;


import com.web.userchat.UserChatApplication;
import com.web.userchat.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClientsfd
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {UserChatApplication.class, WebSocketIntegrationTest.TestSecurityConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    @BeforeEach
    public void setup() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
    }

    @Test
    public void testWebSocketConnection() throws Exception {
        String url = "ws://localhost:%d/chat-websocket";
        StompSession session = stompClient.connect(url, new StompSessionHandlerAdapter() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {

            }
        }).get();

        assertTrue(session.isConnected());
    }
}