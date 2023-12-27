package com.member.memberinquiry.exception;

import com.member.memberinquiry.dto.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDTO<String>> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(ResponseDTO.<String>builder()
                        .success(false)
                        .message(e.getMessage())
                        .status(e.getHttpStatus().name())
                        .code(e.getHttpStatus().value())
                        .build());
    }
}
