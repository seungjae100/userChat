package com.web.userchat.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChattingRoomDTO {
    private String chattingRoomId; // 채팅방 아이디 고유한 아이디
    private String chattingRoomName; // 채팅방 이름

    // 정적 메서드를 이용한 채팅방 생성
    public static ChattingRoomDTO create(String chattingRoomName) {
        ChattingRoomDTO room = new ChattingRoomDTO();
        room.chattingRoomId = UUID.randomUUID().toString(); // 고유한 아이디 생성
        room.chattingRoomName = chattingRoomName;
        return room;
    }
    // 기본 생성자
    public ChattingRoomDTO() {}

}
