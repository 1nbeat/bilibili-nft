package com.arifun.spider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BiliBiliNftDetailSpiderResult {

    private String nftId;
    private Long itemId;
    private boolean saved;
    private String message;
}

