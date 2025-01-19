package com.web.userchat.mapper;

import com.web.userchat.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {

    void save(User user); // 회원 가입 정보를 저장한다.

    Optional<User> findByEmail(String email); // 이메일로 회원을 조회한다.

    Optional<User> findById(Long id); // 회원 고유 아이디로 조회한다.

    List<User> findAll(); // 모든 사용자를 조회한다.

    void update(User user); // 회원 정보를 수정한다.

    void delete(Long id); // 회원 고유 아이디로 회원을 탈퇴한다.

    void updateLikeCount(@Param("userId") Long userId, @Param("increment") boolean increment); // 좋아요 클릭으로 Count 변경
    List<User> findPopularUsers(int limit); // 좋아요 수 기준으로 정렬
}
