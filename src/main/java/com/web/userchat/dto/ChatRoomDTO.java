package com.web.userchat.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChatRoomDTO {
    private String chatRoomId;

    private String chatRoomName;


    // 정적 메서드를 이용한 채팅방 생성
    public static ChatRoomDTO create(String chatRoomName) {
        ChatRoomDTO room = new ChatRoomDTO();
        room.chatRoomId = UUID.randomUUID().toString(); // 고유한 아이디 생성
        room.chatRoomName = chatRoomName;
        return room;
    }
    // 기본 생성자
    public ChatRoomDTO() {}

}
