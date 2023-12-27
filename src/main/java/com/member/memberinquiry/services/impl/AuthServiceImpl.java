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
import com.member.memberinquiry.dto.ResponseDTO;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final MemberRepository memberRepository;
    private final AuthInterceptor authInterceptor;

    @Override
    public ResponseDTO<String> login(LoginRequest loginRequest) {
        Member member = memberRepository.findByCustomerId(loginRequest.getCustomerId())
                .orElseThrow(() -> new CustomException("Member not found", HttpStatus.UNAUTHORIZED));
        UUID uuid = UUID.randomUUID();
        if (member.getPassword().equals(loginRequest.getPassword())) {
            String sessionToken = member.getCustomerId() + "-" + uuid;
            authInterceptor.addToken(sessionToken, member);
            return ResponseDTO.<String>builder()
                    .data(sessionToken)
                    .success(true)
                    .status(HttpStatus.OK.name())
                    .code(HttpStatus.OK.value())
                    .message("Login successful")
                    .build();
        } else {
            throw new CustomException("Login false", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public void logout(HttpServletRequest request) {
        String sessionToken = request.getHeader("Authorization");
        authInterceptor.invalidate(sessionToken);
    }
}
