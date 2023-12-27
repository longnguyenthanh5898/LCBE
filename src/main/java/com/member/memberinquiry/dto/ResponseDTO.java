package com.member.memberinquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO<T> {
    private Boolean success;
    private String message;
    private String status;
    private Integer code;
    private T data;
}
