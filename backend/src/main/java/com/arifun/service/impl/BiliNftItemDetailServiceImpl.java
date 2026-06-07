package com.arifun.service.impl;

import com.arifun.domain.BiliNftAttributeFilter;
import com.arifun.domain.BiliNftItemAttributeFacet;
import com.arifun.domain.BiliNftItemDetail;
import com.arifun.domain.PageResult;
import com.arifun.mapper.BiliNftItemDetailMapper;
import com.arifun.service.BiliNftItemAttributeService;
import com.arifun.service.BiliNftItemDetailService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BiliNftItemDetailServiceImpl implements BiliNftItemDetailService {

    private final BiliNftItemDetailMapper biliNftItemDetailMapper;
    private final BiliNftItemAttributeService biliNftItemAttributeService;

    public BiliNftItemDetailServiceImpl(
            BiliNftItemDetailMapper biliNftItemDetailMapper,
            BiliNftItemAttributeService biliNftItemAttributeService) {
        this.biliNftItemDetailMapper = biliNftItemDetailMapper;
        this.biliNftItemAttributeService = biliNftItemAttributeService;
    }

    /**
     * 按唯一键新增或更新一条藏品详情记录。
     *
     * @param detail 藏品详情
     */
    @Override
    public void upsert(BiliNftItemDetail detail) {
        biliNftItemDetailMapper.upsert(detail);
    }

    /**
     * 按藏品ID查询一条藏品详情记录。
     *
     * @param nftId 藏品ID
     * @return 藏品详情
     */
    @Override
    public BiliNftItemDetail getByNftId(String nftId) {
        return biliNftItemDetailMapper.selectByNftId(nftId);
    }

    /**
     * 查询系列属性筛选面板数据。
     *
     * @param itemId 系列ID
     * @return 属性面板数据
     */
    @Override
    public List<BiliNftItemAttributeFacet> listAttributeFacets(Long itemId) {
        return biliNftItemAttributeService.listFacetsByItemId(itemId);
    }

    /**
     * 按属性条件分页筛选藏品详情。
     *
     * @param itemId 系列ID
     * @param filters 属性条件
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<BiliNftItemDetail> searchByAttributes(
            Long itemId,
            List<BiliNftAttributeFilter> filters,
            Integer pageNum,
            Integer pageSize) {
        int validPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int validPageSize = pageSize == null || pageSize < 1 ? 20 : pageSize;

        List<BiliNftAttributeFilter> validFilters = filters == null ? Collections.emptyList() : filters.stream()
                .filter(filter -> filter != null
                        && filter.getTraitType() != null
                        && !filter.getTraitType().isBlank()
                        && filter.getTraitValue() != null
                        && !filter.getTraitValue().isBlank())
                .toList();

        Long total = biliNftItemAttributeService.countNftIdsByFilters(itemId, validFilters);
        if (total == null || total == 0L) {
            return new PageResult<>(0L, validPageNum, validPageSize, Collections.emptyList());
        }

        List<String> allNftIds = biliNftItemAttributeService.listNftIdsByFilters(itemId, validFilters);
        int fromIndex = Math.min((validPageNum - 1) * validPageSize, allNftIds.size());
        int toIndex = Math.min(fromIndex + validPageSize, allNftIds.size());
        List<String> pageNftIds = allNftIds.subList(fromIndex, toIndex);

        if (pageNftIds.isEmpty()) {
            return new PageResult<>(total, validPageNum, validPageSize, Collections.emptyList());
        }

        List<BiliNftItemDetail> records = biliNftItemDetailMapper.selectByNftIds(pageNftIds);
        return new PageResult<>(total, validPageNum, validPageSize, new ArrayList<>(records));
    }
}
