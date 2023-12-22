package com.member.memberinquiry.exception;

import com.member.memberinquiry.dto.MessageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<MessageDTO> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(MessageDTO.builder()
                        .message(e.getMessage())
                        .build());
    }
}
