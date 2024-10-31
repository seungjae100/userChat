package com.web.userchat.service;

import com.web.userchat.dto.ChattingRoomDTO;
import com.web.userchat.model.ChatMessage;
import com.web.userchat.model.MessageType;
import com.web.userchat.model.User;
import com.web.userchat.repository.ChatMessageRepository;
import com.web.userchat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final Map<String, ChattingRoomDTO> chatRoom = new HashMap<>();
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatService(ChatMessageRepository chatMessageRepository,
                       UserRepository userRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // WebSocket 메시지 처리 메서드 추가
    public ChatMessage processMessage(ChatMessage message) {
        // 메시지 타입 검증
        if (message.getType() == null) {
            message.setType(MessageType.CHAT);
        }

        // 타임스탬프 설정
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }

        // 채팅방 존재 여부 확인
        ChattingRoomDTO room = getChatRoomById(message.getChattingRoomId());
        if (room == null) {
            throw new IllegalArgumentException("존재하지 않는 채팅방입니다.");
        }

        // 발신자 온라인 상태 확인
        if (!isUserOnline(message.getSender())) {
            addUser(message.getSender());
        }

        // 메시지 저장 및 반환
        return chatMessageRepository.save(message);
    }

    // 시스템 메시지 전송 메서드 수정
    public void sendSystemMessage(String roomId, String content) {
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setType(MessageType.SYSTEM);
        systemMessage.setSender("SYSTEM");
        systemMessage.setContent(content);
        systemMessage.setChattingRoomId(roomId);
        systemMessage.setTimestamp(LocalDateTime.now());

        messagingTemplate.convertAndSend("/topic/chat/" + roomId, systemMessage);
    }

    // 사용자 입장 처리 메서드 추가
    public void handleUserJoin(String username, String roomId) {
        addUser(username);
        sendSystemMessage(roomId, username + "님이 입장하셨습니다.");
    }

    // 사용자 퇴장 처리 메서드 추가
    public void handleUserLeave(String username, String roomId) {
        removeUser(username);
        sendSystemMessage(roomId, username + "님이 퇴장하셨습니다.");
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

    public String getOrCreateChatRoomId(String user1, String user2) {
        String sortedUsers = user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
        String chatRoomId = DigestUtils.sha256Hex(sortedUsers);

        chatRoom.computeIfAbsent(chatRoomId, id -> {
            ChattingRoomDTO newRoom = new ChattingRoomDTO();
            newRoom.setChattingRoomId(chatRoomId);
            newRoom.setChattingRoomName(user1 + "와 " + user2 + "의 채팅방");
            return newRoom;
        });
        return chatRoomId;
    }

    public ChattingRoomDTO getChatRoomById(String chatRoomId) {
        return Optional.ofNullable(chatRoom.get(chatRoomId))
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
    }

    public void saveMessage(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getChatMessages(String chattingRoomId) {
        return chatMessageRepository.findByChattingRoomId(chattingRoomId);
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

    public boolean isUserOnline(String username) {
        return onlineUsers.contains(username);
    }
}