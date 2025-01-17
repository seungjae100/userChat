package com.web.userchat.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessage {

    private Long messageId; // 메세지의 고유 아이디 (PK)

    private Long roomId; // 채팅방 고유번호 (FK)
    private Long userId; // 사용자 아이디 (FK)

    private String sender; // 메세지 보낸 사람
    private String message; // 메세지 내용
    private MessageType type; // 메세지 타입;
    private LocalDateTime sendTime; // 메세지 보낸시간
}
