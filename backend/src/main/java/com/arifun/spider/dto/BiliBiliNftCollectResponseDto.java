package com.arifun.spider.dto;

import lombok.Data;

@Data
public class BiliBiliNftCollectResponseDto {

    private Integer code;
    private String message;
    private Integer ttl;
    private BiliBiliNftCollectDataDto data;
}

