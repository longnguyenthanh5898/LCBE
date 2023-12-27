package com.member.memberinquiry.services;

import com.member.memberinquiry.dto.LoginRequest;
import com.member.memberinquiry.dto.ResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    ResponseDTO<String> login(LoginRequest loginRequest);

    void logout(HttpServletRequest request);
}
