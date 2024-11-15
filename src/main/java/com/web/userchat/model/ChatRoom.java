package com.web.userchat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<ChatMessage> messageList = new ArrayList<>();

    public ChatRoom(String chatRoomId, String chatRoomName) {
        this.chatRoomId = chatRoomId;
        this.chatRoomName = chatRoomName;
        this.userCount = 2; // 초기화 시 기본값 설정
    }
}
