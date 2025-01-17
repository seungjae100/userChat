package com.web.userchat.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatRoomDTO {

    private long roomId;
    private String roomName;
    private int userCount;
    private LocalDateTime lastMessageTime;
    private String lastMessage;
}
