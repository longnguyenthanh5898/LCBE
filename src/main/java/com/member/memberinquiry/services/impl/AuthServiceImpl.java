package com.member.memberinquiry.services.impl;

import com.member.memberinquiry.config.AuthInterceptor;
import com.member.memberinquiry.dto.LoginRequest;
import com.member.memberinquiry.entity.Member;
import com.member.memberinquiry.exception.CustomException;
import com.member.memberinquiry.repository.MemberRepository;
import com.member.memberinquiry.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final MemberRepository memberRepository;
    private final AuthInterceptor authInterceptor;

    @Override
    public String login(LoginRequest loginRequest) {
        Member member = memberRepository.findByCustomerId(loginRequest.getCustomerId())
                .orElseThrow(() -> new CustomException("Member not found", HttpStatus.UNAUTHORIZED));
        UUID uuid = UUID.randomUUID();
        if (member.getPassword().equals(loginRequest.getPassword())) {
            String sessionToken = member.getCustomerId() + "-" + uuid;
            authInterceptor.addToken(sessionToken, member);
            return sessionToken;
        } else {
            throw new CustomException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public void logout(HttpServletRequest request) {
        String sessionToken = request.getHeader("Authorization");
        authInterceptor.invalidate(sessionToken);
    }
}
