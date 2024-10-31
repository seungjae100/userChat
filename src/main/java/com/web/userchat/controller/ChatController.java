package com.web.userchat.controller;

import com.web.userchat.dto.ChattingRoomDTO;
import com.web.userchat.model.ChatMessage;
import com.web.userchat.model.MessageType;
import com.web.userchat.model.User;
import com.web.userchat.repository.UserRepository;
import com.web.userchat.service.ChatService;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private UserRepository userRepository;

    // 기존 채팅룸 사용자 조회 메서드 유지
    @GetMapping("/chatRoom")
    public String getAllUsers(Principal principal, Model model) {
        String currentEmail = principal.getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String currentUsername = currentUser.getUsername();
        chatService.loginUser(currentUsername);

        addCommonModelAttributes(model, currentUser, currentUsername);

        List<User> allUsers = chatService.getAllUsersExceptCurrentUser(currentUsername);
        Set<String> onlineUsers = chatService.getOnlineUsers();

        model.addAttribute("allUsers", allUsers);
        model.addAttribute("onlineUsers", onlineUsers);
        return "chatRoom";
    }

    // 기존 채팅방 ID 생성 메서드 유지
    @GetMapping("/api/chatRoom/getId")
    @ResponseBody
    public ResponseEntity<String> getChatRoomId(@RequestParam String user1Email, @RequestParam String user2Email) {
        // 사용자 이메일을 소문자로 변환하고 공백 제거
        String normalizedUser1 = user1Email.trim().toLowerCase();
        String normalizedUser2 = user2Email.trim().toLowerCase();

        // 이메일을 알파벳순으로 정렬하여 결합
        String sortedUsers = normalizedUser1.compareTo(normalizedUser2) < 0
                ? normalizedUser1 + "_" + normalizedUser2
                : normalizedUser2 + "_" + normalizedUser1;

        // 방 ID 생성
        String chatRoomId = DigestUtils.sha256Hex(sortedUsers);
        System.out.println("Generated chatRoomId: " + chatRoomId);

        return ResponseEntity.ok(chatRoomId);
    }

    // 기존 메시지 조회 메서드 유지
    @GetMapping("/api/chatRoom/{chatRoomId}/messages")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getChatMessages(@PathVariable String chatRoomId) {
        List<ChatMessage> messages = chatService.getChatMessages(chatRoomId);
        return ResponseEntity.ok(messages);
    }

    // WebSocket 메시지 처리 메서드 수정
    @MessageMapping("/chat/{chatRoomId}")
    @SendTo("/topic/chat/{chatRoomId}")
    public ChatMessage handleChatMessage(
            @DestinationVariable String chatRoomId,
            @Payload ChatMessage chatMessage,
            Principal principal
    ) {
        // 보안을 위해 실제 발신자 정보로 업데이트
        chatMessage.setSender(principal.getName());
        chatMessage.setChattingRoomId(chatRoomId);
        chatMessage.setTimestamp(LocalDateTime.now());

        // 메시지 저장
        chatService.saveMessage(chatMessage);

        return chatMessage;
    }

    // WebSocket 구독 처리를 위한 새로운 메서드
    @MessageMapping("/chat.subscribe/{chatRoomId}")
    @SendTo("/topic/chat/{chatRoomId}")
    public ChatMessage handleSubscribe(
            @DestinationVariable String chatRoomId,
            Principal principal
    ) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(MessageType.SUBSCRIBE);
        chatMessage.setSender(principal.getName());
        chatMessage.setChattingRoomId(chatRoomId);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setContent(principal.getName() + "님이 채팅방에 입장하셨습니다.");

        return chatMessage;
    }

    // 기존 사용자 검색 메서드 유지
    @GetMapping("/users/search")
    public String searchUsers(@RequestParam("query") String query, Principal principal, Model model) {
        String currentEmail = principal.getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        String currentUsername = currentUser.getUsername();

        List<User> searchResults = chatService.searchUsers(query, currentUsername);
        searchResults.removeIf(user -> user.getUsername().equals(currentUsername));

        addCommonModelAttributes(model, currentUser, currentUsername);
        model.addAttribute("allUsers", searchResults);

        return "chatRoom";
    }

    // 기존 공통 모델 속성 추가 메서드 유지
    private void addCommonModelAttributes(Model model, User currentUser, String currentUsername) {
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("onlineUsers", chatService.getOnlineUsers());
    }
}
