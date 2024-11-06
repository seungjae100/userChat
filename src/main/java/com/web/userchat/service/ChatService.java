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



    @Transactional
    public boolean leaveChatRoom(ChatRoom chatRoom, String email) {

        String username = userRepository.findByEmail(email)
                .map(User::getUsername)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 채팅방 나가기 메세지 생성
        ChatMessage leaveMessage = new ChatMessage();
        leaveMessage.setType(MessageType.SYSTEM);
        leaveMessage.setSender("SYSTEM");
        leaveMessage.setContent(username + "님이 채팅방을 나가셨습니다.");
        leaveMessage.setChatRoom(chatRoom);
        leaveMessage.setTimestamp(LocalDateTime.now());

        // 메세지 저장 및 전송
        chatMessageRepository.save(leaveMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getChatRoomId(), leaveMessage);

        // userCount 감소
        chatRoom.setUserCount(chatRoom.getUserCount() - 1);
        chatRoomRepository.save(chatRoom);

        // userCount 가 0 이 아니면 방 삭제하지 않고 종료
        if (chatRoom.getUserCount() > 0) {
            return false; // 방이 삭제되지 않음
        }
        // userCount 가 0이면 채팅방과 메세지 삭제
        chatMessageRepository.deleteByChatRoom(chatRoom);
        chatRoomRepository.delete(chatRoom);
        return true;
    }


    public void saveMessage(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
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