package com.arifun.controller;

import com.arifun.domain.ApiResponse;
import com.arifun.domain.BiliNftItemAttributeFacet;
import com.arifun.domain.BiliNftItemDetail;
import com.arifun.domain.BiliNftItemDetailSearchRequest;
import com.arifun.domain.PageResult;
import com.arifun.service.BiliNftItemDetailService;
import com.arifun.spider.BiliBiliNftDetailSpiderTask;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/bili-nft-item-details")
public class BiliNftItemDetailController {

    private final BiliNftItemDetailService biliNftItemDetailService;
    private final BiliBiliNftDetailSpiderTask biliBiliNftDetailSpiderTask;

    public BiliNftItemDetailController(
            BiliNftItemDetailService biliNftItemDetailService,
            BiliBiliNftDetailSpiderTask biliBiliNftDetailSpiderTask) {
        this.biliNftItemDetailService = biliNftItemDetailService;
        this.biliBiliNftDetailSpiderTask = biliBiliNftDetailSpiderTask;
    }

    /**
     * 按藏品ID查询藏品详情，不存在时先抓取再返回。
     *
     * @param nftId 藏品ID
     * @return 通用响应
     */
    @GetMapping("/{nftId}")
    public ApiResponse<BiliNftItemDetail> getByNftId(@PathVariable String nftId) {
        BiliNftItemDetail detail = biliNftItemDetailService.getByNftId(nftId);
        if (detail != null) {
            return ApiResponse.success(detail);
        }

        try {
            biliBiliNftDetailSpiderTask.crawl(nftId);
        } catch (Exception e) {
            log.warn("藏品详情现爬失败，nftId={}", nftId, e);
        }

        return ApiResponse.success(biliNftItemDetailService.getByNftId(nftId));
    }

    /**
     * 查询系列属性筛选面板数据。
     *
     * @param itemId 系列ID
     * @return 通用响应
     */
    @GetMapping("/facets")
    public ApiResponse<List<BiliNftItemAttributeFacet>> listAttributeFacets(@RequestParam Long itemId) {
        return ApiResponse.success(biliNftItemDetailService.listAttributeFacets(itemId));
    }

    /**
     * 按属性条件分页筛选藏品详情。
     *
     * @param request 查询请求
     * @return 通用响应
     */
    @PostMapping("/search")
    public ApiResponse<PageResult<BiliNftItemDetail>> searchByAttributes(@RequestBody BiliNftItemDetailSearchRequest request) {
        return ApiResponse.success(
                biliNftItemDetailService.searchByAttributes(
                        request.getItemId(),
                        request.getFilters(),
                        request.getPageNum(),
                        request.getPageSize()));
    }
}
