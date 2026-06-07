package com.arifun.controller;

import com.arifun.domain.ApiResponse;
import com.arifun.domain.BiliNftDetail;
import com.arifun.domain.PageResult;
import com.arifun.service.BiliNftDetailService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bili-nft-details")
public class BiliNftDetailController {

    private final BiliNftDetailService biliNftDetailService;

    public BiliNftDetailController(BiliNftDetailService biliNftDetailService) {
        this.biliNftDetailService = biliNftDetailService;
    }

    /**
     * 新增一条藏品明细记录。
     *
     * @param detail 藏品明细
     * @return 通用响应
     */
    @PostMapping
    public ApiResponse<Void> create(@RequestBody BiliNftDetail detail) {
        biliNftDetailService.create(detail);
        return ApiResponse.success(null);
    }

    /**
     * 按主键更新一条藏品明细记录。
     *
     * @param id 主键ID
     * @param detail 藏品明细
     * @return 通用响应
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody BiliNftDetail detail) {
        detail.setId(id);
        biliNftDetailService.update(detail);
        return ApiResponse.success(null);
    }

    /**
     * 按主键删除一条藏品明细记录。
     *
     * @param id 主键ID
     * @return 通用响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        biliNftDetailService.deleteById(id);
        return ApiResponse.success(null);
    }

    /**
     * 按主键查询一条藏品明细记录。
     *
     * @param id 主键ID
     * @return 通用响应
     */
    @GetMapping("/{id}")
    public ApiResponse<BiliNftDetail> getById(@PathVariable Long id) {
        return ApiResponse.success(biliNftDetailService.getById(id));
    }

    /**
     * 查询全部藏品明细记录。
     *
     * @return 通用响应
     */
    @GetMapping
    public ApiResponse<List<BiliNftDetail>> listAll() {
        return ApiResponse.success(biliNftDetailService.listAll());
    }

    /**
     * 分页查询藏品明细记录。
     *
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 通用响应
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<BiliNftDetail>> listPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(biliNftDetailService.listPage(pageNum, pageSize));
    }

    /**
     * 按系列ID分页查询藏品明细记录。
     *
     * @param itemId 系列ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 通用响应
     */
    @GetMapping("/page/item/{itemId}")
    public ApiResponse<PageResult<BiliNftDetail>> listPageByItemId(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ApiResponse.success(biliNftDetailService.listPageByItemId(itemId, pageNum, pageSize));
    }
}
