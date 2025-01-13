package com.web.userchat.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessage {

    private Long id;
    private String roomId; // 채팅방 번호
    private String sender; // 메세지 보낸 사람
    private String message; // 메세지
    private MessageType type; // 메세지 타입;
    private LocalDateTime sendTime; // 메세지 보낸시간

}
