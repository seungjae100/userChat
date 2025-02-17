package com.web.userchat.controller;

import com.web.userchat.model.ChatMessage;
import com.web.userchat.model.User;
import com.web.userchat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 기존 채팅룸 사용자 조회 메서드 유지
    @GetMapping("/chatRoom")
    public String getAllUsers(Principal principal, Model model) {
        String currentEmail = principal.getName();
        User currentUser = chatService.getCurrentUser(currentEmail);
        chatService.loginUser(currentUser.getUsername());

        model.addAllAttributes(chatService.getCommonModelAttributes(currentEmail));
        model.addAttribute("allUsers", chatService.getAllUserExceptCurrentUser(currentUser.getUsername()));
        model.addAttribute("onlineUsers", chatService.getOnlineUsers());


        return "chatRoom";
    }

    // 기존 채팅방 ID 생성 메서드 유지
    @GetMapping("/api/chatRoom/getId")
    @ResponseBody
    public ResponseEntity<String> getChatRoomId(
            @RequestParam String user1Email,
            @RequestParam String user2Email) {
        return ResponseEntity.ok(chatService.getChatRoomId(user1Email, user2Email));
    }

    @PostMapping("/chat/enter")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> enterChatRoom(
            @RequestParam String chatRoomId,
            Principal principal) {
        return ResponseEntity.ok(chatService.enterChatRoom(chatRoomId, principal.getName()));
    }


    @PostMapping("/chat/leave")
    @ResponseBody
    public ResponseEntity<String> leaveChatRoom(
            @RequestParam String chatRoomId,
            Principal principal) {
        return ResponseEntity.ok(chatService.leaveChatRoom(chatRoomId, principal.getName()));
    }


    // 기존 메시지 조회 메서드 유지
    @GetMapping("/api/chatRoom/{chatRoomId}/messages")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getChatMessages(
            @PathVariable String chatRoomId) {
        return ResponseEntity.ok(chatService.getChatMessages(chatRoomId));
    }

    // WebSocket 메시지 처리 메서드 수정
    @MessageMapping("/chat/{chatRoomId}")
    @SendTo("/topic/chat/{chatRoomId}")
    public ChatMessage handleChatMessage(
            @DestinationVariable String chatRoomId,
            @Payload ChatMessage chatMessage,
            Principal principal) {

        String senderEmail = principal.getName();
        chatMessage.setSender(senderEmail);

        return chatService.handleChatMessage(chatRoomId, chatMessage, senderEmail);
    }


    // 기존 사용자 검색 메서드 유지
    @GetMapping("/users/search")
    public String searchUsers(@RequestParam("query") String query, Principal principal, Model model) {
        String currentEmail = principal.getName();
        User currentUser = chatService.getCurrentUser(currentEmail);
        List<User> searchedUsers = chatService.searchUsers(query, currentUser.getUsername());

        model.addAllAttributes(chatService.getCommonModelAttributes(currentEmail));
        model.addAttribute("allUsers", searchedUsers);

        return "chatRoom";
    }
}
