package com.arifun.service.impl;

import com.arifun.domain.BiliNftImage;
import com.arifun.domain.BiliNftSeries;
import com.arifun.mapper.BiliNftSeriesMapper;
import com.arifun.service.BiliNftDetailService;
import com.arifun.service.BiliNftSeriesService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class BiliNftSeriesServiceImpl implements BiliNftSeriesService {

    private final BiliNftSeriesMapper biliNftSeriesMapper;
    private final BiliNftDetailService biliNftDetailService;

    public BiliNftSeriesServiceImpl(
            BiliNftSeriesMapper biliNftSeriesMapper,
            BiliNftDetailService biliNftDetailService) {
        this.biliNftSeriesMapper = biliNftSeriesMapper;
        this.biliNftDetailService = biliNftDetailService;
    }

    /**
     * 按系列ID查询系列记录。
     *
     * @param itemId 系列ID
     * @return 系列记录
     */
    @Override
    public BiliNftSeries getByItemId(Long itemId) {
        return biliNftSeriesMapper.selectByItemId(itemId);
    }

    /**
     * 当系列不存在时新增一条系列记录。
     *
     * @param series 系列记录
     */
    @Override
    public void saveIfAbsent(BiliNftSeries series) {
        if (series == null || series.getItemId() == null) {
            return;
        }
        if (biliNftSeriesMapper.selectByItemId(series.getItemId()) == null) {
            biliNftSeriesMapper.insert(series);
        }
    }

    /**
     * 查询系列记录列表，支持按系列ID和系列名称筛选。
     *
     * @param itemId 系列ID
     * @param itemName 系列名称
     * @return 系列记录列表
     */
    @Override
    public List<BiliNftSeries> listAll(Long itemId, String itemName) {
        String validItemName = itemName == null ? null : itemName.trim();
        if (validItemName != null && validItemName.isEmpty()) {
            validItemName = null;
        }
        return biliNftSeriesMapper.selectAll(itemId, validItemName);
    }

    /**
     * 按系列ID随机查询指定数量的藏品图片。
     *
     * @param itemId 系列ID
     * @param limit 数量
     * @return 藏品图片列表
     */
    @Override
    public List<BiliNftImage> listRandomImagesByItemId(Long itemId, Integer limit) {
        return biliNftDetailService.listRandomImagesByItemId(itemId, limit);
    }

    /**
     * 按多个系列ID随机查询每个系列指定数量的藏品图片。
     *
     * @param itemIds 系列ID列表
     * @param limit 每个系列的数量
     * @return 按系列ID分组的藏品图片
     */
    @Override
    public Map<Long, List<BiliNftImage>> listRandomImagesByItemIds(List<Long> itemIds, Integer limit) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Map.of();
        }
        List<Long> validItemIds = itemIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (validItemIds.isEmpty()) {
            return Map.of();
        }
        return biliNftDetailService.listRandomImagesByItemIds(validItemIds, limit).stream()
                .filter(image -> image.getItemId() != null)
                .collect(Collectors.groupingBy(BiliNftImage::getItemId));
    }
}
