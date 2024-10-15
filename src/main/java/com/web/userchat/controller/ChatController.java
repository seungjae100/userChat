package com.web.userchat.controller;

import com.web.userchat.dto.ChattingRoomDTO;
import com.web.userchat.model.ChatMessage;
import com.web.userchat.model.User;
import com.web.userchat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Set;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/users")
    public String getAllUsers(Model model) {
        List<User> allUsers = chatService.getAllUsers();
        Set<String> onlineUsers = chatService.getOnlineUsers();

        model.addAttribute("allUsers", allUsers);
        model.addAttribute("onlineUsers", onlineUsers);
        return "userList"; // 유저 목록 페이지 html
    }

    @GetMapping("/chat/{user1}/{user2}")
    public String getChattingRoom(@PathVariable String user1, @PathVariable String user2, Model model) {
        ChattingRoomDTO chattingRoomDTO = chatService.createChattingRoom(user1, user2);
        model.addAttribute("chattingRoomDTO", chattingRoomDTO);
        model.addAttribute("messages", chatService.getChatMessages(chattingRoomDTO.getChattingRoomId()));
        return "chattingRoom";
    }

    @MessageMapping("/chat.sendMessage/{chattingRoomId}")
    @SendTo("/topic/{chattingRoomId}")
    public ChatMessage sendMessage(@PathVariable String chattingRoomId, @Payload ChatMessage chatMessage) {
        chatMessage.setChatroomId(chattingRoomId); // 채팅룸 아이디 저장
        chatService.saveMessage(chatMessage); // 메세지 저장
        return chatMessage;
    }


}
