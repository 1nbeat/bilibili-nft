package com.arifun.controller;

import com.arifun.domain.ApiResponse;
import com.arifun.spider.BiliBiliNftCollectSpiderTask;
import com.arifun.spider.BiliBiliNftDetailSpiderTask;
import com.arifun.spider.dto.BiliBiliNftBatchSpiderResult;
import com.arifun.spider.dto.BiliBiliNftDetailSpiderResult;
import com.arifun.spider.dto.BiliBiliNftSpiderResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spider/bilibili-nft")
public class BiliBiliNftSpiderController {

    private final BiliBiliNftCollectSpiderTask biliBiliNftCollectSpiderTask;
    private final BiliBiliNftDetailSpiderTask biliBiliNftDetailSpiderTask;

    public BiliBiliNftSpiderController(
            BiliBiliNftCollectSpiderTask biliBiliNftCollectSpiderTask,
            BiliBiliNftDetailSpiderTask biliBiliNftDetailSpiderTask) {
        this.biliBiliNftCollectSpiderTask = biliBiliNftCollectSpiderTask;
        this.biliBiliNftDetailSpiderTask = biliBiliNftDetailSpiderTask;
    }

    /**
     * 手动触发指定系列的分页抓取任务。
     *
     * @param itemId 系列ID
     * @param pageSize 每页条数
     * @return 抓取结果
     */
    @PostMapping("/crawl/{itemId}")
    public ApiResponse<BiliBiliNftSpiderResult> crawl(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(biliBiliNftCollectSpiderTask.crawl(itemId, pageSize));
    }

    /**
     * 手动触发系列ID范围批量抓取测试任务。
     *
     * @param startItemId 起始系列ID
     * @param endItemId 结束系列ID
     * @param pageSize 每页条数
     * @return 批量抓取结果
     */
    @PostMapping("/crawl/test/{startItemId}")
    public ApiResponse<BiliBiliNftBatchSpiderResult> batchCrawlTest(
            @PathVariable Long startItemId,
            @RequestParam(defaultValue = "10000") Long endItemId,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return ApiResponse.success(biliBiliNftCollectSpiderTask.batchCrawl(startItemId, endItemId, pageSize));
    }

    /**
     * 手动触发指定藏品详情抓取任务。
     *
     * @param nftId 藏品ID
     * @return 抓取结果
     */
    @PostMapping("/detail/{nftId}")
    public ApiResponse<BiliBiliNftDetailSpiderResult> crawlDetail(@PathVariable String nftId) {
        return ApiResponse.success(biliBiliNftDetailSpiderTask.crawl(nftId));
    }
}
