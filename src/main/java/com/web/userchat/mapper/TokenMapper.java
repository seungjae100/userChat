package com.web.userchat.mapper;

import com.web.userchat.model.Token;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TokenMapper {

    void saveRefreshToken(Token token); // 토큰을 저장한다.

    void deleteRefreshToken(String email); // 토큰을 삭제한다.
}
