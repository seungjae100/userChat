package com.web.userchat.controller;

import com.web.userchat.dto.ChatRoomDTO;
import com.web.userchat.model.ChatRoom;
import com.web.userchat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/chatRooms")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    // 채팅방 생성
    @PostMapping("/create")
    public ResponseEntity<ChatRoomDTO> createChatRoom(
            @RequestParam Long user1Id,
            @RequestParam(required = false) Long user2Id,
            @RequestParam String roomName
    ) {
        ChatRoomDTO chatRoom = chatService.createChatRoom(user1Id, user2Id, roomName);
        return ResponseEntity.ok(chatRoom);
    }

    // 채팅방 그룹으로 업데이트
    @PostMapping("/{roomId}/update-group-status")
    public ResponseEntity<String> updateGroupChatStatus(@PathVariable Long roomId) {
        chatService.updateGroupChatStatus(roomId);
        return ResponseEntity.ok("채팅방이 그룹 채팅으로 업데이트 되었습니다.");
    }

    // 사용자가 참여 중인 채팅방 목록 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatRoomDTO>> getUserChatRooms(@PathVariable Long userId) {
        List<ChatRoomDTO> chatRooms = chatService.getUserChatRooms(userId);
        return ResponseEntity.ok(chatRooms);
    }

    // 특정 채팅방 조회
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoom> getChatRoomById(@PathVariable Long roomId) {
        Optional<ChatRoom> chatRoom = chatService.getChatRoomById(roomId);
        return chatRoom.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // 채팅방 삭제 (나가기)
    @DeleteMapping("/{roomId}")
    public ResponseEntity<String> deleteChatRoom(@PathVariable Long roomId) {
        chatService.deleteChatRoom(roomId);
        return ResponseEntity.ok("채팅방이 삭제되었습니다.");
    }
}
