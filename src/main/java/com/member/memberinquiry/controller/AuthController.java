package com.member.memberinquiry.controller;

import com.member.memberinquiry.dto.LoginRequest;
import com.member.memberinquiry.entity.Member;
import com.member.memberinquiry.services.AuthService;
import com.member.memberinquiry.services.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping("/register")
    public ResponseEntity<Member> createMember(@RequestBody Member member, HttpServletRequest request) {
        return ResponseEntity.ok(memberService.createMember(member, request));
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkCustomerId(@RequestParam(name = "customerId") String customerId) {
        memberService.checkCustomerId(customerId);
        return ResponseEntity.ok().build();
    }

}
