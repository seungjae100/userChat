package com.web.userchat.service;

import com.web.userchat.dto.ChatRoomDTO;
import com.web.userchat.mapper.ChatMessageMapper;
import com.web.userchat.mapper.ChatRoomMapper;
import com.web.userchat.mapper.ChatRoomParticipantMapper;
import com.web.userchat.model.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomMapper chatRoomMapper;
    private final ChatRoomParticipantMapper chatRoomParticipantMapper;
    private final ChatMessageMapper chatMessageMapper;

    // 채팅방 생성
    public ChatRoomDTO createChatRoom(Long user1Id, Long user2Id, String roomName) {
        // 1:1 채팅 중복 체크
        Long existingRoomId = chatRoomMapper.findExistingChatRoom(user1Id, user2Id);
        if (existingRoomId != null) {
            ChatRoom existingRoom = chatRoomMapper.findById(existingRoomId);
            return mapToDTO(existingRoom); // 중복된 방 반환
        }

        // 새로운 채팅방 생성
        ChatRoom newChatRoom = new ChatRoom();
        newChatRoom.setRoomName(roomName);
        newChatRoom.setGroup(false); // 기본적으로 1:1 채팅
        chatRoomMapper.create(newChatRoom);

        // 참여자 추가
        chatRoomParticipantMapper.addParticipant(newChatRoom.getRoomId(), user1Id);
        chatRoomParticipantMapper.addParticipant(newChatRoom.getRoomId(), user2Id);
        return mapToDTO(newChatRoom);
    }

    // 채팅방을 그룹 채팅방으로 전환
    public void updateGroupChatStatus(Long roomId) {
        int participantCount = chatRoomParticipantMapper.countParticipantsByRoomId(roomId);
        if (participantCount >= 3) {
            ChatRoom chatRoom = chatRoomMapper.findById(roomId);
            if (chatRoom != null && !chatRoom.isGroup()) {
                chatRoom.setGroup(true);
                chatRoomMapper.update(chatRoom);
            }
        }
    }

    // 채팅방 조회
    public Optional<ChatRoom> getChatRoomById(Long roomId) {
        return Optional.ofNullable(chatRoomMapper.findById(roomId));
    }

    // 사용자가 참여 중인 채팅방 목록 조회
    public List<ChatRoomDTO> getUserChatRooms(Long userId) {
        List<ChatRoom> chatRooms = chatRoomMapper.findChatRoomsByUserId(userId);
        return chatRooms.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // 채팅방 삭제 (참여자와 메세지 모두 삭제)
    public void deleteChatRoom(Long roomId) {
        // 참여자 정보 삭제
        chatRoomParticipantMapper.deleteParticipantsByRoomId(roomId);
        // 메세지 삭제
        chatMessageMapper.deleteByRoomId(roomId);
        // 채팅방 삭제
        chatRoomMapper.deleteChatRoom(roomId);
    }

    private ChatRoomDTO mapToDTO(ChatRoom chatRoom) {
        ChatRoomDTO dto = new ChatRoomDTO();
        dto.setRoomId(chatRoom.getRoomId());
        dto.setRoomName(chatRoom.getRoomName());
        dto.setLastMessage(chatRoom.getLastMessage());
        dto.setLastMessageTime(chatRoom.getLastMessageTime());

        return dto;
    }


}