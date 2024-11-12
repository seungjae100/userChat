package com.web.userchat.service;

import com.web.userchat.model.ChatMessage;
import com.web.userchat.model.ChatRoom;
import com.web.userchat.model.MessageType;
import com.web.userchat.model.User;
import com.web.userchat.repository.ChatMessageRepository;
import com.web.userchat.repository.ChatRoomRepository;
import com.web.userchat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;

    @Autowired
    public ChatService(ChatMessageRepository chatMessageRepository,
                       UserRepository userRepository,
                       SimpMessagingTemplate messagingTemplate,
                       ChatRoomRepository chatRoomRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.chatRoomRepository = chatRoomRepository;
    }

    public List<User> getAllUsersExceptCurrentUser(String currentUsername) {
        List<User> allUsers = userRepository.findAll();
        allUsers.removeIf(user -> user.getUsername().equals(currentUsername));
        return allUsers;
    }

    public List<User> searchUsers(String username, String currentUsername) {
        if (username == null || username.isEmpty()) {
            return userRepository.findAll().stream()
                    .filter(user -> !user.getUsername().equals(currentUsername))
                    .collect(Collectors.toList());
        }
        return userRepository.findByUsernameContaining(username).stream()
                .filter(user -> !user.getUsername().equals(currentUsername))
                .collect(Collectors.toList());
    }

    public ChatRoom createChatRoom(String chatRoomName, String chatRoomId) {
        ChatRoom chatRoom = new ChatRoom(chatRoomId, chatRoomName);
        chatRoom.setUserCount(2); // 생성 시 userCount를 2로 설정
        chatRoomRepository.save(chatRoom);
        return chatRoom;
    }


    public Map<String, Object> enterChatRoom(String chatRoomId, String username) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // 이메일 형식에서 username 부분만 추출
        String normalizedUsername = username.split("@")[0];

        // 메세지 리스트를 확인하여 사용자가 이전에 채팅방에 있었는지 확인
        boolean isReturningUser = chatRoom.getMessageList().stream()
                .anyMatch(message -> {
                    String messageSender = message.getSender().split("@")[0];
                    return messageSender.equals(normalizedUsername);
                });

        System.out.println("Checking return status for " + username + ": " + isReturningUser); // 디버깅용

        // userCount 업데이트
        if (chatRoom.getUserCount() < 2) {
            chatRoom.setUserCount(chatRoom.getUserCount() + 1);
            chatRoomRepository.save(chatRoom);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("isReturningUser", isReturningUser);
        response.put("userCount", chatRoom.getUserCount());

        return response;
    }

    public String leaveChatRoom(String chatRoomId, String username) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // userCount가 0보다 클 때만 감소
        if (chatRoom.getUserCount() > 0) {
            chatRoom.setUserCount(chatRoom.getUserCount() - 1);
            chatRoomRepository.save(chatRoom);
        }

        if (chatRoom.getUserCount() == 0) {
            chatRoomRepository.delete(chatRoom);
            return "채팅방이 삭제되었습니다.";
        } else {
            return username + "님이 채팅방을 나갔습니다.";
        }
    }


    public void saveMessage(ChatMessage chatMessage) {
        if (chatMessage != null) {
            chatMessageRepository.save(chatMessage);
        } else {
            System.out.println("ChatMessage가 null입니다.");
        }
    }

    public List<ChatMessage> getChatMessages(ChatRoom chatRoom) {
        return chatMessageRepository.findByChatRoom(chatRoom);
    }

    public void loginUser(String username) {
        addUser(username);
    }

    public void logoutUser(String username) {
        removeUser(username);
    }

    public void addUser(String username) {
        onlineUsers.add(username);
    }

    public void removeUser(String username) {
        onlineUsers.remove(username);
    }

    public Set<String> getOnlineUsers() {
        return onlineUsers;
    }
}