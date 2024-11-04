package com.web.userchat.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(indexes = @Index(columnList = "chatRoomId"))
public class ChatRoom {

    @Id
    @Column(length = 64)
    private String chatRoomId; // 채팅방 아이디

    @Column(nullable = false)
    private String chatRoomName; // 채팅방 이름

    private int userCount; // 채팅방에 있는 유저의 수

    public ChatRoom(String chatRoomId, String chatRoomName) {
        this.chatRoomId = chatRoomId;
        this.chatRoomName = chatRoomName;
    }
}
