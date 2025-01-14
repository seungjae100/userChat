package com.web.userchat.mapper;

import com.web.userchat.model.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    void save(ChatMessage chatMessage); //  메세지를 저장한다.

    List<ChatMessage> findByRoomId(Long roomId); // 채팅방 고유 아이디로 조회한다.

    void deleteByRoomId(Long roomId); // 채팅방 고유 아이디로 삭제한다.
}
