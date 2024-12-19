//package com.web.userchat.service;
//
//import com.web.userchat.model.ChatMessage;
//import com.web.userchat.repository.ChatMessageRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//class ChatServiceTest {
//
//    @Mock
//    private ChatMessageRepository chatMessageRepository;
//
//    @InjectMocks
//    private  ChatService chatService;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void createChattingRoom() {
//        // given
//        String user1 = "user1";
//        String user2 = "user2";
//
//        // when
//        var chatRoom = chatService.createChattingRoom(user1, user2);
//
//        // then
//        assertNotNull(chatRoom);
//        assertEquals("user1 and user2",  chatRoom.getChattingRoomName());
//
//    }
//
//    @Test
//    void saveMessage() {
//
//        // given
//        ChatMessage chatMessage = new ChatMessage("user1", "user2", "Hello", "roomId1");
//
//        // when
//        chatService.saveMessage(chatMessage);
//
//        // then
//        verify(chatMessageRepository, times(1)).save(chatMessage);
//    }
//
//    @Test
//    void isUserOnline() {
//        // given
//        String user = "user1";
//        chatService.addUser(user);
//
//        // when
//        boolean isOnline = chatService.isUserOnline(user);
//
//        // then
//        assertTrue(isOnline);
//
//    }
//}