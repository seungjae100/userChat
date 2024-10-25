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

    @GetMapping("/chatRoom")
    public String getAllUsers(Principal principal, Model model) {

        String currentEmail = principal.getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String currentUsername = currentUser.getUsername();
        chatService.loginUser(currentUsername); // 로그인 시 사용자 온라인 설정

        List<User> allUsers = chatService.getAllUsersExceptCurrentUser(currentUsername);
        Set<String> onlineUsers = Optional.ofNullable(chatService.getOnlineUsers()).orElse(Collections.emptySet());

        ChattingRoomDTO chattingRoomDTO = new ChattingRoomDTO();
        chattingRoomDTO.setChattingRoomId(UUID.randomUUID().toString());
        model.addAttribute("chattingRoomDTO", chattingRoomDTO);


        model.addAttribute("allUsers", allUsers);
        model.addAttribute("onlineUsers", onlineUsers);
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("currentEmail", currentEmail);
        return "chatRoom"; // 유저 목록 페이지 html
    }

    @GetMapping("/chatRoom/{chatRoomId}")
    public String getChattingRoom(@PathVariable String chatRoomId, Model model) {
        ChattingRoomDTO chattingRoomDTO = chatService.getChatRoomById(chatRoomId);

        model.addAttribute("chattingRoomDTO", chattingRoomDTO);
        model.addAttribute("message", chatService.getChatMessages(chatRoomId));
        return "chatRoom";
    }


    @GetMapping("/api/chat/{chattingRoomId}")
    public ResponseEntity<List<ChatMessage>> getChatMessages(@PathVariable String chattingRoomId) {
        List<ChatMessage> messages = chatService.getChatMessages(chattingRoomId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/api/chat/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage chatMessage) {
        // 채팅 메세지를 저장
        chatService.saveMessage(chatMessage);

        // 저장된 메세지를 응답으로 반환
        return ResponseEntity.ok(chatMessage);
    }

    @MessageMapping("/chat.sendMessage/{chattingRoomId}")
    @SendTo("/topic/{chattingRoomId}")
    public ChatMessage sendMessage(@PathVariable String chattingRoomId, @Payload ChatMessage chatMessage) {
        chatMessage.setChattingRoomId(chattingRoomId); // 채팅룸 아이디 저장
        chatService.saveMessage(chatMessage); // 메세지 저장
        return chatMessage;
    }

    @GetMapping("/users/search")
    public String searchUsers(@RequestParam("query") String query, Principal principal, Model model) {
        // 현재 사용자 이메일을 가져옴
        String currentEmail = principal.getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        String currentUsername = currentUser.getUsername();

        // 검색어에 따른 사용자 목록 가져오기
        List<User> searchResults = userRepository.findByUsernameContaining(query);
        // 현재 로그인한 사용자는 검색 결과에서 제외
        searchResults.removeIf(user -> user.getUsername().equals(currentUsername));

        // 모델에 필요한 정보 추가
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("allUsers", searchResults);
        model.addAttribute("onlineUsers", chatService.getOnlineUsers());

        return "chatRoom"; // 유저 목록 페이지 html
    }

}
