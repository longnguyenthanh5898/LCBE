package com.member.memberinquiry.controller;

import com.member.memberinquiry.dto.PagingDTO;
import com.member.memberinquiry.entity.Member;
import com.member.memberinquiry.services.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.member.memberinquiry.dto.ResponseDTO;
import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MemberController {
    private final MemberService memberService;


    @PutMapping("/members/{id}")
    public ResponseEntity<ResponseDTO<Member>> updateMember(@PathVariable(name = "id") Long id,
                                               @RequestBody Member member,
                                               HttpServletRequest request) {
        return ResponseEntity.ok(memberService.updateMember(id, member, request));
    }

    @GetMapping("/members")
    public ResponseEntity<ResponseDTO<PagingDTO<List<Member>>>> getMembers(@RequestParam(name = "page", defaultValue = "1") int page,
                                                             @RequestParam(name = "size", defaultValue = "5") int size,
                                                             @RequestParam(name = "customerId", required = false) String customerId,
                                                             @RequestParam(name = "name", required = false) String name,
                                                             @RequestParam(name = "email", required = false) String email,
                                                             @RequestParam(name = "phoneNumber", required = false) String phoneNumber,
                                                             @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date startDate,
                                                             @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date endDate) {
        return ResponseEntity.ok(memberService.getMembers(page, size, customerId, name, email, phoneNumber, startDate, endDate));
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<ResponseDTO<String>> deleteMember(@PathVariable(name = "id") Long id, HttpServletRequest request) {
        memberService.deleteMember(id, request);
        return ResponseEntity.ok(ResponseDTO.<String>builder()
                .data("Member deleted successfully")
                .success(true)
                .status(HttpStatus.OK.name())
                .code(HttpStatus.OK.value())
                .build());
    }
    
//    @GetMapping("/members/export")
//    public ResponseEntity<ResponseDTO<ByteArrayResource>> exportMembers(@RequestParam(name = "customerId", required = false) String customerId,
//                                           @RequestParam(name = "name", required = false) String name,
//                                           @RequestParam(name = "email", required = false) String email,
//                                           @RequestParam(name = "phoneNumber", required = false) String phoneNumber,
//                                           @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date startDate,
//                                           @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date endDate) {
//        byte[] bytes = memberService.exportMembers(customerId, name, email, phoneNumber, startDate, endDate);
//        ByteArrayResource resource = new ByteArrayResource(bytes);
//        return ResponseEntity.ok()
//                .contentLength(bytes.length)
//                .header("Content-Type", "application/octet-stream")
//                .header("Content-Disposition", "attachment; filename=Members.xlsx")
//                .body(ResponseDTO.<ByteArrayResource>builder()
//                        .data(resource)
//                        .success(true)
//                        .status(HttpStatus.OK.name())
//                        .code(HttpStatus.OK.value())
//                        .message("Members exported successfully")
//                        .build());
//
//
//    }
@GetMapping("/members/export")
public ResponseEntity<?> exportMembers(@RequestParam(name = "customerId", required = false) String customerId,
                                       @RequestParam(name = "name", required = false) String name,
                                       @RequestParam(name = "email", required = false) String email,
                                       @RequestParam(name = "phoneNumber", required = false) String phoneNumber,
                                       @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date startDate,
                                       @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date endDate) {
    byte[] bytes = memberService.exportMembers(customerId, name, email, phoneNumber, startDate, endDate);
    ByteArrayResource resource = new ByteArrayResource(bytes);
    return ResponseEntity.ok()
            .contentLength(bytes.length)
            .header("Content-Type", "application/octet-stream")
            .header("Content-Disposition", "attachment; filename=Members.xlsx")
            .body(resource);

}
}
