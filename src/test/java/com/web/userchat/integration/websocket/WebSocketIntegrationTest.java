package com.web.userchat.integration.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class WebSocketIntegrationTest {

    private WebSocketStompClient stompClient;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setup() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
    }

    @Test
    @WithMockUser(username = "testUser")
    public void testWebSocketConnection() throws Exception {
        String url = String.format("ws://localhost:%d/chat-websocket", port);
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