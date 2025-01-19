package com.web.userchat.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatRoom {

    private Long roomId; // 채팅방 고유 아이디 (PK)
    private Long userId; // 채팅방 생성자 (FK)
    private String roomName; // 채팅방 이름
    private boolean isGroup; // 그룹채팅의 여부
    private LocalDateTime createdAt; // 채팅방이 만들어진 시간
    private LocalDateTime lastMessageTime; // 마지막 메세지 시간
    private String lastMessage; // 마지막으로 보낸 메세지


}
