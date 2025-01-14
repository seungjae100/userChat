//package com.web.userchat.service;
//
//import com.web.userchat.model.ChatMessage;
//import com.web.userchat.model.ChatRoom;
//import com.web.userchat.model.User;
//import com.web.userchat.repository.ChatMessageRepository;
//import com.web.userchat.repository.ChatRoomRepository;
//import com.web.userchat.repository.UserRepository;
//import org.apache.commons.codec.digest.DigestUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
//@Service
//@Transactional
//public class ChatService {
//
//    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
//    private final ChatMessageRepository chatMessageRepository;
//    private final UserRepository userRepository;
//    private final SimpMessagingTemplate messagingTemplate;
//    private final ChatRoomRepository chatRoomRepository;
//
//    @Autowired
//    public ChatService(ChatMessageRepository chatMessageRepository,
//                       UserRepository userRepository,
//                       SimpMessagingTemplate messagingTemplate,
//                       ChatRoomRepository chatRoomRepository) {
//        this.chatMessageRepository = chatMessageRepository;
//        this.userRepository = userRepository;
//        this.messagingTemplate = messagingTemplate;
//        this.chatRoomRepository = chatRoomRepository;
//    }
//
//    // 채팅방 ID 생성 및 관리 관련
//    public String getChatRoomId(String user1Email, String user2Email) {
//        String normalizedUser1 = user1Email.trim().toLowerCase();
//        String normalizedUser2 = user2Email.trim().toLowerCase();
//
//        String sortedUsers = normalizedUser1.compareTo(normalizedUser2) < 0
//                ? normalizedUser1 + "_" + normalizedUser2
//                : normalizedUser2 + "_" + normalizedUser1;
//
//        String chatRoomId = DigestUtils.sha256Hex(sortedUsers);
//
//        chatRoomRepository.findById(chatRoomId)
//                .orElseGet(() -> createChatRoom(
//                        String.format("Chat between %s and %s", normalizedUser1, normalizedUser2),
//                        chatRoomId
//                ));
//
//        return chatRoomId;
//    }
//
//    public ChatRoom createChatRoom(String chatRoomName, String chatRoomId) {
//        ChatRoom chatRoom = new ChatRoom(chatRoomId, chatRoomName);
//        chatRoom.setUserCount(2);
//        return chatRoomRepository.save(chatRoom);
//    }
//
//    // 메시지 처리 관련
//    public ChatMessage handleChatMessage(String chatRoomId, ChatMessage chatMessage, String principalName) {
//        ChatRoom chatRoom = getChatRoomOrThrow(chatRoomId);
//        String username = getUsernameFromEmail(principalName);
//
//        chatMessage.setChatRoom(chatRoom);
//        chatMessage.setSender(username);
//        chatMessage.setTimestamp(LocalDateTime.now());
//
//        return saveMessage(chatMessage);
//    }
//
//    public ChatMessage saveMessage(ChatMessage chatMessage) {
//        if (chatMessage == null) {
//            throw new IllegalArgumentException("채팅메세지가 null 일 수 없습니다.");
//        }
//        return chatMessageRepository.save(chatMessage);
//    }
//
//    public List<ChatMessage> getChatMessages(String chatRoomId) {
//        ChatRoom chatRoom = getChatRoomOrThrow(chatRoomId);
//        return chatMessageRepository.findByChatRoom(chatRoom);
//    }
//
//    // 사용자 관리 관련
//    public User getCurrentUser(String email) {
//        return userRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("사용자의 이메일을 찾을 수 없습니다." + email));
//    }
//
//    public Map<String, Object> getCommonModelAttributes(String email) {
//        User cureentUser = getCurrentUser(email);
//        Map<String, Object> attributes = new HashMap<>();
//        attributes.put("currentUsername", cureentUser.getUsername());
//        attributes.put("currentUser", cureentUser);
//        attributes.put("onlineUsers", getOnlineUsers());
//
//        return attributes;
//    }
//
//    public List<User> getAllUserExceptCurrentUser(String currentUsername) {
//        List<User> allUsers = userRepository.findAll();
//
//        List<User> filteredUsers = allUsers.stream()
//                        .filter(user -> !user.getUsername().equals(currentUsername))
//                        .collect(Collectors.toList());
//        return filteredUsers;
//    }
//
//    public List<User> searchUsers(String query, String currentUsername) {
//        if (query == null || query.isEmpty()) {
//            return getAllUserExceptCurrentUser(currentUsername);
//        }
//        return userRepository.findByUsernameContaining(query).stream()
//                .filter(user -> !user.getUsername().equals(currentUsername))
//                .collect(Collectors.toList());
//    }
//
//    // 채팅 입/퇴장 관련
//    public Map<String, Object> enterChatRoom(String chatRoomId, String username) {
//        ChatRoom chatRoom = getChatRoomOrThrow(chatRoomId);
//        String normalizedUsername = username.split("@")[0];
//
//        boolean isReturningUser = chatRoom.getMessageList().stream()
//                .anyMatch(message ->  {
//                    String messageSender = message.getSender().split("@")[0];
//                    return messageSender.equals(normalizedUsername);
//                });
//
//        if (chatRoom.getUserCount() < 2) {
//            chatRoom.setUserCount(chatRoom.getUserCount() + 1);
//            chatRoomRepository.save(chatRoom);
//        }
//
//        return Map.of(
//                "isReturningUser", isReturningUser,
//                "userCount", chatRoom.getUserCount()
//        );
//    }
//
//    public String leaveChatRoom(String chatRoomId, String username) {
//        ChatRoom chatRoom = getChatRoomOrThrow(chatRoomId);
//
//        if (chatRoom.getUserCount() > 0) {
//            chatRoom.setUserCount(chatRoom.getUserCount() - 1);
//            chatRoomRepository.save(chatRoom);
//        }
//        if (chatRoom.getUserCount() == 0) {
//            chatRoomRepository.delete(chatRoom);
//            return "채팅방이 삭제되었습니다.";
//        }
//        return username + "님이 채팅방을 나갔습니다.";
//    }
//
//    // 유틸리티 메서드
//    private ChatRoom getChatRoomOrThrow(String chatRoomId) {
//        return chatRoomRepository.findById(chatRoomId)
//                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
//    }
//
//    private String getUsernameFromEmail(String email) {
//        return userRepository.findByEmail(email)
//                .map(User::getUsername)
//                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//    }
//
//    // 온라인 사용자 관리
//    public void loginUser(String username) {
//        onlineUsers.add(username);
//    }
//
//    public void logoutUser(String username) {
//        onlineUsers.remove(username);
//    }
//
//    public Set<String> getOnlineUsers() {
//        return onlineUsers;
//    }
//}