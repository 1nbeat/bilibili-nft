package com.arifun.spider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BiliBiliNftSpiderResult {

    private Long itemId;
    private Integer pageSize;
    private Integer crawledPages;
    private Integer savedCount;
    private String stopReason;
}

