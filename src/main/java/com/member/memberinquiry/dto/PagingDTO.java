package com.member.memberinquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class PagingDTO<T> {
    private T resource;
    private int page;
    private long totalPages;
    private long totalElements;
}
