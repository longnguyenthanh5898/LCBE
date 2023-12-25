package com.member.memberinquiry;

import com.member.memberinquiry.entity.Member;
import com.member.memberinquiry.entity.Role;
import com.member.memberinquiry.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@SpringBootApplication
@RequiredArgsConstructor
public class MemberInquiryApplication {
    private final MemberRepository memberRepository;

    public static void main(String[] args) {
        SpringApplication.run(MemberInquiryApplication.class, args);
    }

    @PostConstruct
    public void init() {
        Member admin = memberRepository.findByCustomerId("210601").orElse(Member.builder()
                .customerId("210601")
                .password("P@zzw0rd216")
                .role(Role.ROLE_ADMIN)
                .build());
        memberRepository.save(admin);
    }
}
