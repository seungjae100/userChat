package com.web.userchat.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender; // 발신자
    private String receiver; // 수신자
    private String content; // 내용
    private String chattingRoomId; // 메세지가 어떤 채팅방에 속하는지에 대한 아이디

    // 기본 생성자
    public ChatMessage() {}

    public ChatMessage(String sender, String receiver, String content, String chattingRoomId) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.chattingRoomId = chattingRoomId;
    }
}
