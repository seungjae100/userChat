package com.web.userchat.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomParticipant {

    private Long participantId; // 고유 아이디
    private Long roomId; // 채팅방 ID (FK)
    private Long userId; // 사용자 ID (FK)
    private boolean isActive; // 사용자가 지금 채팅방에 활성 상태인지의 여부
}
