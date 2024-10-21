package com.web.userchat.controller;

import com.web.userchat.dto.ChattingRoomDTO;
import com.web.userchat.model.ChatMessage;
import com.web.userchat.model.User;
import com.web.userchat.repository.UserRepository;
import com.web.userchat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;
import java.util.Set;

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
        Set<String> onlineUsers = chatService.getOnlineUsers();


        model.addAttribute("allUsers", allUsers);
        model.addAttribute("onlineUsers", onlineUsers);
        model.addAttribute("currentUser", currentUsername);
        return "chatRoom"; // 유저 목록 페이지 html
    }

    @GetMapping("/chatRoom/{user1}/{user2}")
    public String getChattingRoom(@PathVariable String user1, @PathVariable String user2, Model model) {
        ChattingRoomDTO chattingRoomDTO = chatService.createChattingRoom(user1, user2);
        model.addAttribute("chattingRoomDTO", chattingRoomDTO);
        model.addAttribute("messages", chatService.getChatMessages(chattingRoomDTO.getChattingRoomId()));
        return "chatRoom";
    }

    @MessageMapping("/chat.sendMessage/{chattingRoomId}")
    @SendTo("/topic/{chattingRoomId}")
    public ChatMessage sendMessage(@PathVariable String chattingRoomId, @Payload ChatMessage chatMessage) {
        chatMessage.setChattingRoomId(chattingRoomId); // 채팅룸 아이디 저장
        chatService.saveMessage(chatMessage); // 메세지 저장
        return chatMessage;
    }


}
