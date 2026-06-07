package com.arifun.mapper;

import com.arifun.domain.BiliNftSeries;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BiliNftSeriesMapper {

    /**
     * 按系列ID查询系列记录。
     *
     * @param itemId 系列ID
     * @return 系列记录
     */
    BiliNftSeries selectByItemId(@Param("itemId") Long itemId);

    /**
     * 新增一条系列记录。
     *
     * @param series 系列记录
     * @return 影响行数
     */
    int insert(BiliNftSeries series);

    /**
     * 查询系列记录列表，支持按系列ID和系列名称筛选。
     *
     * @param itemId 系列ID
     * @param itemName 系列名称
     * @return 系列记录列表
     */
    List<BiliNftSeries> selectAll(
            @Param("itemId") Long itemId,
            @Param("itemName") String itemName);
}
