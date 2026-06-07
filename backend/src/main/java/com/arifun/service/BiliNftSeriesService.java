package com.arifun.service;

import com.arifun.domain.BiliNftImage;
import com.arifun.domain.BiliNftSeries;
import java.util.List;
import java.util.Map;

public interface BiliNftSeriesService {

    /**
     * 按系列ID查询系列记录。
     *
     * @param itemId 系列ID
     * @return 系列记录
     */
    BiliNftSeries getByItemId(Long itemId);

    /**
     * 当系列不存在时新增一条系列记录。
     *
     * @param series 系列记录
     */
    void saveIfAbsent(BiliNftSeries series);

    /**
     * 查询系列记录列表，支持按系列ID和系列名称筛选。
     *
     * @param itemId 系列ID
     * @param itemName 系列名称
     * @return 系列记录列表
     */
    List<BiliNftSeries> listAll(Long itemId, String itemName);

    /**
     * 按系列ID随机查询指定数量的藏品图片。
     *
     * @param itemId 系列ID
     * @param limit 数量
     * @return 藏品图片列表
     */
    List<BiliNftImage> listRandomImagesByItemId(Long itemId, Integer limit);

    /**
     * 按多个系列ID随机查询每个系列指定数量的藏品图片。
     *
     * @param itemIds 系列ID列表
     * @param limit 每个系列的数量
     * @return 按系列ID分组的藏品图片
     */
    Map<Long, List<BiliNftImage>> listRandomImagesByItemIds(List<Long> itemIds, Integer limit);
}
