package com.web.userchat.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ChatRoom {

    private Long roomId; // 채팅방 고유 아이디
    private String roomName; // 채팅방 이름
    private int userCount; // 채팅방 인원 수
    private LocalDateTime createdAt; // 채팅방이 만들어진 시간

    /* 1 대 다수의 관계*/

    private List<ChatMessage> chatMessages;
}
