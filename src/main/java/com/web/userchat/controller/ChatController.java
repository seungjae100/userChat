package com.web.userchat.controller;

import com.web.userchat.dto.ChattingRoomDTO;
import com.web.userchat.model.ChatMessage;
import com.web.userchat.model.User;
import com.web.userchat.repository.UserRepository;
import com.web.userchat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private UserRepository userRepository;


    // 채팅룸에 모든 사용자를 찾는 메서드
    @GetMapping("/chatRoom")
    public String getAllUsers(Principal principal, Model model) {

        String currentEmail = principal.getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String currentUsername = currentUser.getUsername();
        chatService.loginUser(currentUsername); // 로그인 시 사용자 온라인 설정

        addCommonModelAttributes(model, currentUser, currentUsername);

        List<User> allUsers = chatService.getAllUsersExceptCurrentUser(currentUsername);
        Set<String> onlineUsers = chatService.getOnlineUsers();

        model.addAttribute("allUsers", allUsers);
        model.addAttribute("onlineUsers", onlineUsers);
        return "chatRoom"; // 유저 목록 페이지 html
    }

    @GetMapping("/api/chatRoom/getId")
    public ResponseEntity<String> getChatRoomId(@RequestParam String user1, @RequestParam String user2) {
        String chatRoomId = chatService.getOrCreateChatRoomId(user1, user2);
        return ResponseEntity.ok(chatRoomId);
    }

    // 메세지 조회를 위한 메서드
    @GetMapping("/api/chatRoom/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessage>> getChatMessages(@PathVariable String chatRoomId) {
        List<ChatMessage> messages = chatService.getChatMessages(chatRoomId);
        return ResponseEntity.ok(messages);
    }

    // 채팅방에 있는 채팅을 저장하는 메서드
    @MessageMapping("/chat/send/{chatRoomId}")
    @SendTo("/topic/{chattingRoomId}")
    public ChatMessage sendMessage(@PathVariable String chatRoomId, @Payload ChatMessage chatMessage) {
        chatMessage.setChattingRoomId(chatRoomId); // 채팅룸 아이디 저장
        chatService.saveMessage(chatMessage); // 메세지 저장
        return chatMessage;
    }

    // 유저를 검색하여 찾는 메서드
    @GetMapping("/users/search")
    public String searchUsers(@RequestParam("query") String query, Principal principal, Model model) {
        // 현재 사용자 이메일을 가져옴
        String currentEmail = principal.getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        String currentUsername = currentUser.getUsername();

        // 검색어에 따른 사용자 목록 가져오기
        List<User> searchResults = chatService.searchUsers(query, currentUsername);
        // 현재 로그인한 사용자는 검색 결과에서 제외
        searchResults.removeIf(user -> user.getUsername().equals(currentUsername));

        addCommonModelAttributes(model, currentUser, currentUsername);
        model.addAttribute("allUsers", searchResults);

        return "chatRoom"; // 유저 목록 페이지 html
    }

    // 모델에 공통 속성 추가를 위한 메서드
    private void addCommonModelAttributes(Model model, User currentUser, String currentUsername) {
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("onlineUsers", chatService.getOnlineUsers());
    }

}
