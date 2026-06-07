package com.arifun.mapper;

import com.arifun.domain.BiliNftAttributeFilter;
import com.arifun.domain.BiliNftItemAttribute;
import com.arifun.domain.BiliNftItemAttributeOption;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BiliNftItemAttributeMapper {

    /**
     * 批量新增或更新属性明细。
     *
     * @param attributes 属性明细列表
     * @return 影响行数
     */
    int batchUpsert(@Param("attributes") List<BiliNftItemAttribute> attributes);

    /**
     * 按藏品ID删除属性明细。
     *
     * @param nftId 藏品ID
     * @return 影响行数
     */
    int deleteByNftId(@Param("nftId") String nftId);

    /**
     * 按系列ID查询属性面板选项。
     *
     * @param itemId 系列ID
     * @return 属性选项列表
     */
    List<BiliNftItemAttributeOption> selectFacetOptionsByItemId(@Param("itemId") Long itemId);

    /**
     * 按属性条件筛选藏品ID列表。
     *
     * @param itemId 系列ID
     * @param filters 属性条件
     * @return 藏品ID列表
     */
    List<String> selectNftIdsByFilters(
            @Param("itemId") Long itemId,
            @Param("filters") List<BiliNftAttributeFilter> filters);

    /**
     * 按属性条件统计藏品数量。
     *
     * @param itemId 系列ID
     * @param filters 属性条件
     * @return 藏品数量
     */
    Long countNftIdsByFilters(
            @Param("itemId") Long itemId,
            @Param("filters") List<BiliNftAttributeFilter> filters);
}
