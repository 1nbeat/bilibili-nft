package com.arifun.spider.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiliBiliNftBatchSpiderResult {

    private Long startItemId;
    private Long endItemId;
    private Integer pageSize;
    private Integer successCount;
    private Integer failedCount;
    private Long lastProcessedItemId;
    private List<Long> failedItemIds;
}
