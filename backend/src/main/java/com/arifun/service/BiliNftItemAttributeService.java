package com.arifun.service;

import com.arifun.domain.BiliNftAttributeFilter;
import com.arifun.domain.BiliNftItemAttribute;
import com.arifun.domain.BiliNftItemAttributeFacet;
import java.util.List;

public interface BiliNftItemAttributeService {

    /**
     * 重建指定藏品的属性明细。
     *
     * @param nftId 藏品ID
     * @param attributes 属性明细列表
     */
    void rebuildByNftId(String nftId, List<BiliNftItemAttribute> attributes);

    /**
     * 查询系列属性筛选面板数据。
     *
     * @param itemId 系列ID
     * @return 属性面板数据
     */
    List<BiliNftItemAttributeFacet> listFacetsByItemId(Long itemId);

    /**
     * 按属性条件筛选藏品ID。
     *
     * @param itemId 系列ID
     * @param filters 属性条件
     * @return 藏品ID列表
     */
    List<String> listNftIdsByFilters(Long itemId, List<BiliNftAttributeFilter> filters);

    /**
     * 按属性条件统计藏品数量。
     *
     * @param itemId 系列ID
     * @param filters 属性条件
     * @return 藏品数量
     */
    Long countNftIdsByFilters(Long itemId, List<BiliNftAttributeFilter> filters);
}
