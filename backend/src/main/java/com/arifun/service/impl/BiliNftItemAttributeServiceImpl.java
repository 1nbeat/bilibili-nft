package com.arifun.service.impl;

import com.arifun.domain.BiliNftAttributeFilter;
import com.arifun.domain.BiliNftItemAttribute;
import com.arifun.domain.BiliNftItemAttributeFacet;
import com.arifun.domain.BiliNftItemAttributeOption;
import com.arifun.mapper.BiliNftItemAttributeMapper;
import com.arifun.service.BiliNftItemAttributeService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BiliNftItemAttributeServiceImpl implements BiliNftItemAttributeService {

    private final BiliNftItemAttributeMapper biliNftItemAttributeMapper;

    public BiliNftItemAttributeServiceImpl(BiliNftItemAttributeMapper biliNftItemAttributeMapper) {
        this.biliNftItemAttributeMapper = biliNftItemAttributeMapper;
    }

    /**
     * 重建指定藏品的属性明细。
     *
     * @param nftId 藏品ID
     * @param attributes 属性明细列表
     */
    @Override
    @Transactional
    public synchronized void rebuildByNftId(String nftId, List<BiliNftItemAttribute> attributes) {
        biliNftItemAttributeMapper.deleteByNftId(nftId);
        if (attributes == null || attributes.isEmpty()) {
            return;
        }
        Map<String, BiliNftItemAttribute> uniqueAttributes = new LinkedHashMap<>();
        for (BiliNftItemAttribute attribute : attributes) {
            if (attribute == null) {
                continue;
            }
            uniqueAttributes.put(attributeKey(attribute), attribute);
        }

        List<BiliNftItemAttribute> sortedAttributes = uniqueAttributes.values().stream()
                .sorted(Comparator
                        .comparing(BiliNftItemAttribute::getNftId, Comparator.nullsLast(String::compareTo))
                        .thenComparing(BiliNftItemAttribute::getTraitType, Comparator.nullsLast(String::compareTo))
                        .thenComparing(BiliNftItemAttribute::getTraitValue, Comparator.nullsLast(String::compareTo)))
                .toList();
        if (sortedAttributes.isEmpty()) {
            return;
        }
        biliNftItemAttributeMapper.batchUpsert(sortedAttributes);
    }

    private String attributeKey(BiliNftItemAttribute attribute) {
        return attribute.getNftId() + "\u0001"
                + attribute.getTraitType() + "\u0001"
                + attribute.getTraitValue();
    }

    /**
     * 查询系列属性筛选面板数据。
     *
     * @param itemId 系列ID
     * @return 属性面板数据
     */
    @Override
    public List<BiliNftItemAttributeFacet> listFacetsByItemId(Long itemId) {
        List<BiliNftItemAttributeOption> rows = biliNftItemAttributeMapper.selectFacetOptionsByItemId(itemId);
        Map<String, List<BiliNftItemAttributeOption>> grouped = new LinkedHashMap<>();

        for (BiliNftItemAttributeOption row : rows) {
            String raw = row.getTraitValue();
            String[] parts = raw == null ? new String[0] : raw.split("\\|\\|", 2);
            String traitType = parts.length > 0 ? parts[0] : "";
            String traitValue = parts.length > 1 ? parts[1] : "";
            grouped.computeIfAbsent(traitType, key -> new ArrayList<>())
                    .add(new BiliNftItemAttributeOption(traitValue, row.getCount()));
        }

        List<BiliNftItemAttributeFacet> facets = new ArrayList<>();
        for (Map.Entry<String, List<BiliNftItemAttributeOption>> entry : grouped.entrySet()) {
            facets.add(new BiliNftItemAttributeFacet(entry.getKey(), entry.getValue()));
        }
        return facets;
    }

    /**
     * 按属性条件筛选藏品ID。
     *
     * @param itemId 系列ID
     * @param filters 属性条件
     * @return 藏品ID列表
     */
    @Override
    public List<String> listNftIdsByFilters(Long itemId, List<BiliNftAttributeFilter> filters) {
        return biliNftItemAttributeMapper.selectNftIdsByFilters(itemId, filters);
    }

    /**
     * 按属性条件统计藏品数量。
     *
     * @param itemId 系列ID
     * @param filters 属性条件
     * @return 藏品数量
     */
    @Override
    public Long countNftIdsByFilters(Long itemId, List<BiliNftAttributeFilter> filters) {
        return biliNftItemAttributeMapper.countNftIdsByFilters(itemId, filters);
    }
}
