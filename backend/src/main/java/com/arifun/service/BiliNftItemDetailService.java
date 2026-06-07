package com.arifun.service;

import com.arifun.domain.BiliNftAttributeFilter;
import com.arifun.domain.BiliNftItemAttributeFacet;
import com.arifun.domain.BiliNftItemDetail;
import com.arifun.domain.PageResult;
import java.util.List;

public interface BiliNftItemDetailService {

    /**
     * 按唯一键新增或更新一条藏品详情记录。
     *
     * @param detail 藏品详情
     */
    void upsert(BiliNftItemDetail detail);

    /**
     * 按藏品ID查询一条藏品详情记录。
     *
     * @param nftId 藏品ID
     * @return 藏品详情
     */
    BiliNftItemDetail getByNftId(String nftId);

    /**
     * 查询系列属性筛选面板数据。
     *
     * @param itemId 系列ID
     * @return 属性面板数据
     */
    List<BiliNftItemAttributeFacet> listAttributeFacets(Long itemId);

    /**
     * 按属性条件分页筛选藏品详情。
     *
     * @param itemId 系列ID
     * @param filters 属性条件
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<BiliNftItemDetail> searchByAttributes(
            Long itemId,
            List<BiliNftAttributeFilter> filters,
            Integer pageNum,
            Integer pageSize);
}
