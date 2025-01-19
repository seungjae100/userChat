package com.web.userchat.mapper;

import com.web.userchat.model.ChatRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatRoomMapper {

    // 채팅방을 생성
    void create(ChatRoom chatRoom);

    // 채팅방 삭제
    void deleteChatRoom(@Param("roomId") Long roomId);

    // 채팅방 조회
    ChatRoom findById(@Param("roomId") Long roomId);

    // 채팅방 업데이트 (그룹 여부의 변경)
    void update(ChatRoom chatRoom);

    // 1:1 채팅 중복 방지 조회
    Long findExistingChatRoom(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    // 사용자가 참여 중인 채팅방 조회
    List<ChatRoom> findChatRoomsByUserId(@Param("userId") Long userId);

}
