package com.web.userchat.mapper;

import com.web.userchat.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatRoomParticipantMapper {

    // 활성 상태 업데이트
    void updateActiveStatus(@Param("roomId") Long roomId, @Param("userId") Long userId, @Param("isActive") boolean isActive);

    // 활성 상태 사용자 조회
    List<User> findActiveParticipantsByRoomId(@Param("roomId") Long roomId);

    // 채팅방에 사용자 추가하기
    void addParticipant(@Param("roomId") Long roomId, @Param("userId") Long userId);

    // 특정 채팅방의 참여자 수 조회
    int countParticipantsByRoomId(@Param("roomId") Long roomId);
}
