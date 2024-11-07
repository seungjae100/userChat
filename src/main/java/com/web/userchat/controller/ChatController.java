package com.web.userchat.controller;

import com.web.userchat.model.ChatMessage;
import com.web.userchat.model.ChatRoom;
import com.web.userchat.model.MessageType;
import com.web.userchat.model.User;
import com.web.userchat.repository.ChatRoomRepository;
import com.web.userchat.repository.UserRepository;
import com.web.userchat.service.ChatService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    @Autowired
    private ChatRoomRepository chatRoomRepository;

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
        System.out.println("채팅방 ID : " + chatRoomId);

        // 데이터베이스에 해당 채팅방이 존재하는지 확인
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findById(chatRoomId);
        if (existingChatRoom.isPresent()) {
            System.out.println("이미 채팅방이 존재합니다 : " + chatRoomId);
            return ResponseEntity.ok(chatRoomId); // 이미 존재하면 기존 ID 반환합니다.
        }

        // 존재하지 않으면 새 채팅방 생성
        chatService.createChatRoom("Chat between " + normalizedUser1 + " and " + normalizedUser2, chatRoomId);
        System.out.println("New chat room created with ID: " + chatRoomId);

        return ResponseEntity.ok(chatRoomId);
    }


    // 채팅방 나가기
    @PostMapping("/chat/leave")
    @ResponseBody
    public ResponseEntity<String> leaveChatRoom(@RequestParam String chatRoomId, Principal principal) {
        String currentUsername = principal.getName();

        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(chatRoomId);
        if (chatRoomOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("채팅방을 찾을 수 없습니다.");
        }

        ChatRoom chatRoom = chatRoomOptional.get();
        boolean isDeleted = chatService.leaveChatRoom(chatRoom, currentUsername);

        // 단순 성공 응답만 반환
        return ResponseEntity.ok("채팅방에서 나갔습니다.");
    }



    // 기존 메시지 조회 메서드 유지
    @GetMapping("/api/chatRoom/{chatRoomId}/messages")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getChatMessages(@PathVariable String chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        List<ChatMessage> messages = chatService.getChatMessages(chatRoom);
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
        // chatRoomId로 ChatRoom 객체 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // ChatMessage 객체에 chatRoom 설정
        chatMessage.setChatRoom(chatRoom);

        // sender 필드를 username 으로 설정
        String username = userRepository.findByEmail(principal.getName())
                        .map(User::getUsername)
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 보안을 위해 실제 발신자 정보로 업데이트
        chatMessage.setSender(username);
        chatMessage.setTimestamp(LocalDateTime.now());


        // 메시지 저장
        chatService.saveMessage(chatMessage);

        return chatMessage;
    }


//    // WebSocket 구독 처리를 위한 새로운 메서드
//    @MessageMapping("/chat.subscribe/{chatRoomId}")
//    @SendTo("/topic/chat/{chatRoomId}")
//    public ChatMessage handleSubscribe(
//            @DestinationVariable String chatRoomId,
//            Principal principal
//
//    ) {
//        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
//                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
//
//        ChatMessage chatMessage = new ChatMessage();
//        chatMessage.setType(MessageType.SUBSCRIBE);
//        chatMessage.setSender(principal.getName());
//        chatMessage.setChatRoom(chatRoom);
//        chatMessage.setTimestamp(LocalDateTime.now());
//        chatMessage.setContent(principal.getName() + "님이 채팅방에 입장하셨습니다.");
//
//        return chatMessage;
//    }

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
