package com.web.userchat.mapper;

import com.web.userchat.model.ChatRoom;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ChatRoomMapper {

    void save(ChatRoom chatRoom); // 채팅방을 저장한다.

    Optional<ChatRoom> findById(Long roomId); // 채팅방을 고유아이디로 조회한다.

    List<ChatRoom> findUserWithChatRooms(Long userId); // 사용자의 채팅방 목록 조회

    List<ChatRoom> findAll(); // 채팅방 전체를 조회한다.

    void updateUserCount(Long roomId, int userCount); // 채팅방을 고유아이디를 식별하여 유저 수를 수정한다.

    void delete(Long roomId); // 채팅방의 고유아이디로 삭제한다.
}
