package com.web.userchat.dto;

import com.web.userchat.model.MessageType;

public class ChatMessageDTO {

    private Long id;
    private MessageType type;
    private String sender;
    private String receiver;
    private String content;
    private String chatRoomId; // chatRoomId 만 포함

}
