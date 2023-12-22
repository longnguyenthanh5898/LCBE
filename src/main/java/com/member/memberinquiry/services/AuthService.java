package com.member.memberinquiry.services;

import com.member.memberinquiry.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    String login(LoginRequest loginRequest);

    void logout(HttpServletRequest request);
}
