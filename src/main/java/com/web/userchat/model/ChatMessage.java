package com.web.userchat.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private MessageType type; // 메세지타입 (CHAT, SYSTEM, SUBSCRIBE)
    private String sender; // 발신자
    private String receiver; // 수신자
    private String content; // 메세지 내용
    private String chattingRoomId; // 채팅방 ID
    private LocalDateTime timestamp; // 전송 시간

    public ChatMessage(Long id, MessageType type, String sender, String receiver, String content, String chattingRoomId, LocalDateTime timestamp) {
        this.id = id;
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.chattingRoomId = chattingRoomId;
        this.timestamp = timestamp;
    }
}
