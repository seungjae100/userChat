package com.web.userchat.service;

import com.web.userchat.dto.ChattingRoomDTO;
import com.web.userchat.model.ChatMessage;
import com.web.userchat.model.User;
import com.web.userchat.repository.ChatMessageRepository;
import com.web.userchat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService {

    // 채팅방 저장소
    private final Map<String, ChattingRoomDTO> chatRoom = new HashMap<>(); // 채팅방 DTO
    private final Set<String> onlineUsers = Collections.synchronizedSet(new HashSet<>());
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    public ChatService(ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    // 전체 유저 목록 가져오기 (DB 에서 조회)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 특정 두 유저간의 채팅방을 생성하거나 이미 존재하는 경우 찾는 메서드
    public ChattingRoomDTO createChattingRoom(String user1, String user2) {
        String chattingRoomId = generateRoomId(user1, user2);
        if (!chatRoom.containsKey(chattingRoomId)) {
            ChattingRoomDTO newRoom = ChattingRoomDTO.create(user1 + " and " + user2);
            newRoom.setChattingRoomId(chattingRoomId);
            chatRoom.put(chattingRoomId, newRoom);
        }
        return chatRoom.get(chattingRoomId);
    }

    // 특정 패턴으로 채팅방 ID 생성 ( 두 유저들의 이름을 이용한 아이디 생성 )
    private String generateRoomId(String user1, String user2) {
        List<String> users = Arrays.asList(user1, user2);
        Collections.sort(users);
        return String.join("_", users);
    }

    // 메세지 저장 (DB)
    public void saveMessage(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
    }

    // 특정 채팅방의 메세지 가져오기 (DB 에서 조회)
    public List<ChatMessage> getChatMessages(String chattingRoomId) {
        return chatMessageRepository.findByChattingRoomId(chattingRoomId);
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
