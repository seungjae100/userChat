package com.web.userchat.controller;

import com.web.userchat.dto.ChattingRoomDTO;
import com.web.userchat.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ChatController.class)
class ChatControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatservice;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getAllUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("userList"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getChattingRoom() throws Exception {
        String user1 = "user1";
        String user2 = "user2";
        ChattingRoomDTO mockRoom = new ChattingRoomDTO();
        mockRoom.setChattingRoomId("user1_user2");

        given(chatservice.createChattingRoom(user1, user2)).willReturn(mockRoom);

        mockMvc.perform(get("/chat/{user1}/{user2}", user1, user2))
                .andExpect(status().isOk())
                .andExpect(view().name("chattingRoom"));

    }
}