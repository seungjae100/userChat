package com.web.userchat.service;

import com.web.userchat.dto.ChatMessageDTO;
import com.web.userchat.mapper.ChatMessageMapper;
import com.web.userchat.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;

    // 메세지 저장
    public ChatMessageDTO saveMessage(Long roomId, String sender,String message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(roomId);
        chatMessage.setSender(sender);
        chatMessage.setMessage(message);
        chatMessage.setSendTime(LocalDateTime.now());

        chatMessageMapper.save(chatMessage);
        return mapToDTO(chatMessage);
    }

    // 메세지 조회하기
    public List<ChatMessageDTO> getMessagesByRoomId(Long roomId) {
        List<ChatMessage> messages = chatMessageMapper.findByRoomId(roomId);
        return messages.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // 채팅방 ID 로 모든 메세지 삭제
    public void deleteMessageByRoomId(Long roomId) {
        chatMessageMapper.deleteByRoomId(roomId);
    }

    // 특정 사용자가 보낸 모든 메세지 삭제
    public void deleteMessageByUser(String sender) {
        chatMessageMapper.deleteByUser(sender);
    }

    private ChatMessageDTO mapToDTO(ChatMessage chatMessage) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setMessageId(chatMessage.getMessageId());
        dto.setRoomId(chatMessage.getRoomId());
        dto.setSender(chatMessage.getSender());
        dto.setMessage(chatMessage.getMessage());
        dto.setSendTime(chatMessage.getSendTime());
        return dto;
    }
}
