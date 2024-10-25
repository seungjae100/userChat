package com.web.userchat.service;

import com.web.userchat.dto.ChattingRoomDTO;
import com.web.userchat.model.ChatMessage;
import com.web.userchat.model.User;
import com.web.userchat.repository.ChatMessageRepository;
import com.web.userchat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.codec.digest.DigestUtils;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatService {

    // 채팅방 저장소
    private final Map<String, ChattingRoomDTO> chatRoom = new HashMap<>(); // 채팅방 DTO
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    public ChatService(ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    // 전체 유저 목록 가져오기 (로그인한 사용자 제외)
    public List<User> getAllUsersExceptCurrentUser(String currentUsername) {
        List<User> allUsers = userRepository.findAll();
        allUsers.removeIf(user -> user.getUsername().equals(currentUsername));
        return allUsers;
    }

    // 유저 검색 로직
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

    // 채팅방을 고유한 ID 로 생성하고 저장하거나 이미 존재하는 경우 찾는 메서드
    public ChattingRoomDTO createChattingRoom(String chattingRoomName) {
        ChattingRoomDTO newRoom = ChattingRoomDTO.create(chattingRoomName);
        chatRoom.put(newRoom.getChattingRoomId(), newRoom);
        return newRoom;
    }

    // 채팅방 ID 로 가져오는 메서드 (존재 여부 확인용)
    public ChattingRoomDTO getChatRoomById(String chatRoomId) {
        return Optional.ofNullable(chatRoom.get(chatRoomId))
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
    }

    // 메세지 저장 (DB)
    public void saveMessage(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
    }

    // 특정 채팅방의 메세지 가져오기 (DB 에서 조회)
    public List<ChatMessage> getChatMessages(String chattingRoomId) {
        return chatMessageRepository.findByChattingRoomId(chattingRoomId);
    }

    // 로그인 시 사용자를 온라인 상태로 설정
    public void loginUser(String username) {
        addUser(username);
    }

    public void logoutUser(String username) {
        onlineUsers.remove(username);
    }

    // 접속 유저 관리
    public void addUser(String username) {
        onlineUsers.add(username);
    }

    public void removeUser(String username) {
        onlineUsers.remove(username);
    }

    public Set<String> getOnlineUsers() {
        return onlineUsers;
    }

    // 특정 유저가 온라인인지 확인
    public boolean isUserOnline(String username) {
        return onlineUsers.contains(username);
    }
}
