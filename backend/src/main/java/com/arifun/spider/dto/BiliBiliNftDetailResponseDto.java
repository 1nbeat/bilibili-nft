package com.arifun.spider.dto;

import lombok.Data;

@Data
public class BiliBiliNftDetailResponseDto {

    private Integer code;
    private String message;
    private Integer ttl;
    private BiliBiliNftDetailDataDto data;
}

