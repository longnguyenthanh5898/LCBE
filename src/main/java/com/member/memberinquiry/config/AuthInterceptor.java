package com.member.memberinquiry.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.member.memberinquiry.entity.Member;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final Cache<String, Member> registrationTokensCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String sessionToken = request.getHeader("Authorization");
        if (StringUtils.isEmpty(sessionToken)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return false;
        }
        Member member = registrationTokensCache.getIfPresent(sessionToken);
        if (Objects.isNull(member)) {
            log.error("Unauthorized");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return false;
        } else {
            return true;
        }
    }

    public void addToken(String token, Member member) {
        registrationTokensCache.put(token, member);
    }

    public void invalidate(String sessionToken) {
        registrationTokensCache.invalidate(sessionToken);
    }

    public Member getMember(String sessionToken) {
        return registrationTokensCache.getIfPresent(sessionToken);
    }
}
