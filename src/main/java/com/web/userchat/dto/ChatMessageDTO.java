package com.web.userchat.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageDTO {

    private Long messageId;
    private Long roomId;
    private String sender;
    private String message;
    private LocalDateTime sendTime;
}
