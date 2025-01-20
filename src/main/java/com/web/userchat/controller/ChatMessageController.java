package com.web.userchat.controller;

import com.web.userchat.dto.ChatMessageDTO;
import com.web.userchat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate simpMessagingTemplate; // 실시간 메세지 전송에 사용

    // 실시간 메세지 송수신 처리
    @MessageMapping("/chat")
    @SendTo("/topic/room/{roomId}")
    public ChatMessageDTO handleMessage(ChatMessageDTO chatMessageDTO) {
        // 메세지 저장
        ChatMessageDTO saveMessage = chatMessageService.saveMessage(
                chatMessageDTO.getRoomId(),
                chatMessageDTO.getSender(),
                chatMessageDTO.getMessage()
        );

        // 브로드캐스트 (구독 중인 모든 클라이언트에 전송)
        simpMessagingTemplate.convertAndSend("/topic/room/" + chatMessageDTO.getRoomId(), saveMessage);

        return saveMessage;
    }

    // 채팅방 아이디 기반 메세지 조회
    @GetMapping("/room/{roomId}")
    public List<ChatMessageDTO> getMessagesByRoomId(@PathVariable Long roomId) {
        return chatMessageService.getMessagesByRoomId(roomId);
    }

    // 채팅방 아이디 기반 메세지 삭제
    @DeleteMapping("/room/{roomId}")
    public void deleteMessagesByRoomId(@PathVariable Long roomId) {
        chatMessageService.deleteMessageByRoomId(roomId);
    }
}
