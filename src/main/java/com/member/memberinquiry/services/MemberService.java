package com.member.memberinquiry.services;

import com.member.memberinquiry.dto.PagingDTO;
import com.member.memberinquiry.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface MemberService {
    Member createMember(Member member, HttpServletRequest request);

    Member updateMember(Long id, Member member, HttpServletRequest request);

    PagingDTO<List<Member>> getMembers(int page, int size, String customerId, String name, String email, String phoneNumber, Date startDate, Date endDate);

    void deleteMember(Long id, HttpServletRequest request);

    byte[] exportMembers(String customerId, String name, String email, String phoneNumber, Date startDate, Date endDate);

    void checkCustomerId(String customerId);
}
