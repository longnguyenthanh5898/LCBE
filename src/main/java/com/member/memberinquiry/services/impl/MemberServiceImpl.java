package com.member.memberinquiry.services.impl;

import com.member.memberinquiry.config.AuthInterceptor;
import com.member.memberinquiry.dto.PagingDTO;
import com.member.memberinquiry.entity.Member;
import com.member.memberinquiry.entity.Role;
import com.member.memberinquiry.exception.CustomException;
import com.member.memberinquiry.repository.MemberRepository;
import com.member.memberinquiry.services.MemberService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.member.memberinquiry.dto.ResponseDTO;
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final AuthInterceptor authInterceptor;





    @Override
    public ResponseDTO<Member> createMember(Member member, HttpServletRequest request) {
        Optional<Member> memberOptionalId = memberRepository.findByCustomerId(member.getCustomerId());
        if (memberOptionalId.isPresent()) {
            throw new CustomException("Member already exists", HttpStatus.BAD_REQUEST);
        }
        Optional<Member> memberOptionalEmail = memberRepository.findByEmail(member.getEmail());
        if (memberOptionalEmail.isPresent()) {
            throw new CustomException("Email already exists", HttpStatus.BAD_REQUEST);
        }
        Optional<Member> memberOptionalPhone = memberRepository.findByPhoneNumber(member.getPhoneNumber());
        if (memberOptionalPhone.isPresent()) {
            throw new CustomException("Phone Number already exists", HttpStatus.BAD_REQUEST);
        }
        member.setRole(Role.ROLE_USER);
        memberRepository.save(member);
        return ResponseDTO.<Member>builder()
                .success(true)
                .data(member)
                .message("Member created successfully")
                .status(HttpStatus.OK.name())
                .code(HttpStatus.OK.value())
                .build();

    }

    @Override
    public ResponseDTO<Member>  updateMember(Long id, Member memberDTO, HttpServletRequest request) {
        String sessionToken = request.getHeader("Authorization");
        Member memberToken = authInterceptor.getMember(sessionToken);
        Member member = memberRepository.findById(id).orElseThrow(() -> new CustomException("Member not found", HttpStatus.NOT_FOUND));
        if (Role.ROLE_ADMIN.equals(memberToken.getRole()) || memberToken.getId().equals(id)) {
            member.setPhoneNumber(Objects.nonNull(memberDTO.getPhoneNumber()) ? memberDTO.getPhoneNumber() : member.getPhoneNumber());
            member.setEmail(Objects.nonNull(memberDTO.getEmail()) ? memberDTO.getEmail() : member.getEmail());
            member.setName(Objects.nonNull(memberDTO.getName()) ? memberDTO.getName() : member.getName());
            member.setPassword(Objects.nonNull(memberDTO.getPassword()) ? memberDTO.getPassword() : member.getPassword());
            memberRepository.save(member);
            return ResponseDTO.<Member>builder()
                    .success(true)
                    .data(member)
                    .message("Member updated successfully")
                    .status(HttpStatus.OK.name())
                    .code(HttpStatus.OK.value())
                    .build();

        } else {
            throw new CustomException("Do not have permission to edit member", HttpStatus.FORBIDDEN);
        }

    }

    @Override
    public ResponseDTO<PagingDTO<List<Member>>> getMembers(int page, int size, String customerId, String name, String email, String phoneNumber, Date startDate, Date endDate) {
        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "createdDate");
        PageRequest paging = PageRequest.of(page - 1, size, sort);
        Specification<Member> byRole = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("role"), Role.ROLE_USER);
        Specification<Member> byCustomerId = StringUtils.isEmpty(customerId) ? null : (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("customerId")), "%" + customerId.toLowerCase() + "%");
        Specification<Member> byName = StringUtils.isEmpty(name) ? null : (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        Specification<Member> byEmail = StringUtils.isEmpty(email) ? null : (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        Specification<Member> byPhoneNumber = StringUtils.isEmpty(phoneNumber) ? null : (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), "%" + phoneNumber.toLowerCase() + "%");
        Specification<Member> byCreatedDate = Objects.isNull(startDate) || Objects.isNull(endDate) ? null : (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startDate),
                        criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endDate));
        Page<Member> memberPage = memberRepository.findAll(Specification.where(byCustomerId).and(byName).and(byEmail).and(byPhoneNumber).and(byCreatedDate).and(byRole), paging);
        return ResponseDTO.<PagingDTO<List<Member>>>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK.name())
                .message("Get members successfully")
                .data(PagingDTO.<List<Member>>builder()
                        .resource(memberPage.getContent())
                        .page(page)
                        .totalPages(memberPage.getTotalPages())
                        .totalElements(memberPage.getTotalElements())
                        .build())
                .build();
    }

    @Override
    public void deleteMember(Long id, HttpServletRequest request) {
        String sessionToken = request.getHeader("Authorization");
        Member memberToken = authInterceptor.getMember(sessionToken);
        if (Role.ROLE_ADMIN.equals(memberToken.getRole())) {
            Member member = memberRepository.findById(id).orElseThrow(() -> new CustomException("Member not found", HttpStatus.NOT_FOUND));
            memberRepository.delete(member);
        } else {
            throw new CustomException("Do not have permission to delete member", HttpStatus.FORBIDDEN);
        }

    }

    @Override
    public byte[] exportMembers(String customerId, String name, String email, String phoneNumber, Date startDate, Date endDate) {
        Specification<Member> byRole = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("role"), Role.ROLE_USER);
        Specification<Member> byCustomerId = StringUtils.isEmpty(customerId) ? null : (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("customerId")), "%" + customerId.toLowerCase() + "%");
        Specification<Member> byName = StringUtils.isEmpty(name) ? null : (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        Specification<Member> byEmail = StringUtils.isEmpty(email) ? null : (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        Specification<Member> byPhoneNumber = StringUtils.isEmpty(phoneNumber) ? null : (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), "%" + phoneNumber.toLowerCase() + "%");
        Specification<Member> byCreatedDate = Objects.isNull(startDate) || Objects.isNull(endDate) ? null : (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startDate),
                        criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endDate));
        List<Member> members = memberRepository.findAll(Specification.where(byCustomerId).and(byName).and(byEmail).and(byPhoneNumber).and(byCreatedDate).and(byRole));
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Members");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        CellStyle titleStyle = createCellStyle(workbook, HorizontalAlignment.CENTER, false, true, 14);
        int rowIndex = 0;
        Row mergedRow = sheet.createRow(rowIndex);
        Cell mergedCell = mergedRow.createCell(0);
        mergedCell.setCellValue("Member List");
        mergedCell.setCellStyle(titleStyle);
        rowIndex++;
        List<String> infoHeaders = List.of("MemberNo", "ID", "Name",  "Email","Mobile phone", "Join Date");
        Row infoRow = sheet.createRow(rowIndex);
        for (int i = 0; i < infoHeaders.size(); i++) {
            Cell cell = infoRow.createCell(i);
            cell.setCellValue(infoHeaders.get(i));
            cell.setCellStyle(createCellStyle(workbook, HorizontalAlignment.CENTER, true, true, 12));
        }
        rowIndex++;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            Row row = sheet.createRow(rowIndex);
            row.createCell(0).setCellValue(i + 1);
            row.getCell(0).setCellStyle(createCellStyle(workbook, HorizontalAlignment.CENTER, false, false, 12));
            row.createCell(1).setCellValue(member.getCustomerId());
            row.getCell(1).setCellStyle(createCellStyle(workbook, HorizontalAlignment.CENTER, false, false, 12));
            row.createCell(2).setCellValue(member.getName());
            row.getCell(2).setCellStyle(createCellStyle(workbook, HorizontalAlignment.LEFT, false, false, 12));
            row.createCell(3).setCellValue(member.getEmail());
            row.getCell(3).setCellStyle(createCellStyle(workbook, HorizontalAlignment.CENTER, false, false, 12));
            row.createCell(4).setCellValue(member.getPhoneNumber());
            row.getCell(4).setCellStyle(createCellStyle(workbook, HorizontalAlignment.CENTER, false, false, 12));
            row.createCell(5).setCellValue(dateFormat.format(member.getCreatedDate()));
            row.getCell(5).setCellStyle(createCellStyle(workbook, HorizontalAlignment.CENTER, false, false, 12));
            rowIndex++;
        }
        for (int i = 0; i <= infoHeaders.size(); i++) {
            sheet.autoSizeColumn(i);
        }
        try {
            return workbookToByteArray(workbook);
        } catch (IOException e) {
            throw new CustomException("Export members failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void checkCustomerId(String customerId) {
        Optional<Member> member = memberRepository.findByCustomerId(customerId);
        if (member.isPresent()) {
            throw new CustomException("Customer id already exits", HttpStatus.BAD_REQUEST);
        }
    }

    private CellStyle createCellStyle(Workbook workbook, HorizontalAlignment alignment, Boolean border, Boolean bold, int fontSize) {
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(bold);
        font.setFontHeightInPoints((short) fontSize);
        if (border) {
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
        }
        cellStyle.setAlignment(alignment);
        cellStyle.setFont(font);
        return cellStyle;
    }

    private byte[] workbookToByteArray(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
