package com.arifun.controller;

import com.arifun.domain.ApiResponse;
import com.arifun.domain.BiliNftImage;
import com.arifun.domain.BiliNftSeries;
import com.arifun.service.BiliNftSeriesService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bili-nft-series")
public class BiliNftSeriesController {

    private final BiliNftSeriesService biliNftSeriesService;

    public BiliNftSeriesController(BiliNftSeriesService biliNftSeriesService) {
        this.biliNftSeriesService = biliNftSeriesService;
    }

    /**
     * 按系列ID查询系列记录。
     *
     * @param itemId 系列ID
     * @return 通用响应
     */
    @GetMapping("/{itemId}")
    public ApiResponse<BiliNftSeries> getByItemId(@PathVariable Long itemId) {
        return ApiResponse.success(biliNftSeriesService.getByItemId(itemId));
    }

    /**
     * 查询系列记录列表，支持按系列ID和系列名称筛选，结果随机排序。
     *
     * @param itemId 系列ID
     * @param itemName 系列名称
     * @return 通用响应
     */
    @GetMapping
    public ApiResponse<List<BiliNftSeries>> listAll(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) String itemName) {
        return ApiResponse.success(biliNftSeriesService.listAll(itemId, itemName));
    }

    /**
     * 按多个系列ID批量随机查询每个系列指定数量的藏品图片。
     *
     * @param itemIds 系列ID列表
     * @param limit 每个系列的图片数量
     * @return 通用响应
     */
    @GetMapping("/images")
    public ApiResponse<Map<Long, List<BiliNftImage>>> listRandomImagesByItemIds(
            @RequestParam List<Long> itemIds,
            @RequestParam(defaultValue = "3") Integer limit) {
        return ApiResponse.success(biliNftSeriesService.listRandomImagesByItemIds(itemIds, limit));
    }

    /**
     * 按系列ID随机查询指定数量的藏品图片。
     *
     * @param itemId 系列ID
     * @return 通用响应
     */
    @GetMapping("/{itemId}/images")
    public ApiResponse<List<BiliNftImage>> listRandomImagesByItemId(@PathVariable Long itemId) {
        return ApiResponse.success(biliNftSeriesService.listRandomImagesByItemId(itemId, 3));
    }
}
