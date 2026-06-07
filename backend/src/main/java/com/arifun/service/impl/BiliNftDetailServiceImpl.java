package com.arifun.service.impl;

import com.arifun.domain.BiliNftDetail;
import com.arifun.domain.BiliNftImage;
import com.arifun.domain.PageResult;
import com.arifun.mapper.BiliNftDetailMapper;
import com.arifun.service.BiliNftDetailService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BiliNftDetailServiceImpl implements BiliNftDetailService {

    private final BiliNftDetailMapper biliNftDetailMapper;

    public BiliNftDetailServiceImpl(BiliNftDetailMapper biliNftDetailMapper) {
        this.biliNftDetailMapper = biliNftDetailMapper;
    }

    /**
     * 新增一条藏品明细记录。
     *
     * @param detail 藏品明细
     */
    @Override
    public void create(BiliNftDetail detail) {
        biliNftDetailMapper.insert(detail);
    }

    /**
     * 按唯一键新增或更新一条藏品明细记录。
     *
     * @param detail 藏品明细
     */
    @Override
    public void upsert(BiliNftDetail detail) {
        biliNftDetailMapper.upsert(detail);
    }

    /**
     * 按主键更新一条藏品明细记录。
     *
     * @param detail 藏品明细
     */
    @Override
    public void update(BiliNftDetail detail) {
        biliNftDetailMapper.updateById(detail);
    }

    /**
     * 按主键删除一条藏品明细记录。
     *
     * @param id 主键ID
     */
    @Override
    public void deleteById(Long id) {
        biliNftDetailMapper.deleteById(id);
    }

    /**
     * 按系列ID删除全部藏品明细记录。
     *
     * @param itemId 系列ID
     */
    @Override
    public void deleteByItemId(Long itemId) {
        biliNftDetailMapper.deleteByItemId(itemId);
    }

    /**
     * 按主键查询一条藏品明细记录。
     *
     * @param id 主键ID
     * @return 藏品明细
     */
    @Override
    public BiliNftDetail getById(Long id) {
        return biliNftDetailMapper.selectById(id);
    }

    /**
     * 查询全部藏品明细记录。
     *
     * @return 藏品明细列表
     */
    @Override
    public List<BiliNftDetail> listAll() {
        return biliNftDetailMapper.selectAll();
    }

    /**
     * 按系列ID分页查询藏品明细记录。
     *
     * @param itemId 系列ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<BiliNftDetail> listPageByItemId(Long itemId, Integer pageNum, Integer pageSize) {
        int validPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int validPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int offset = (validPageNum - 1) * validPageSize;
        Long total = biliNftDetailMapper.countByItemId(itemId);
        List<BiliNftDetail> records = biliNftDetailMapper.selectPageByItemId(itemId, offset, validPageSize);
        return new PageResult<>(total, validPageNum, validPageSize, records);
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
        int validLimit = limit == null || limit < 1 ? 5 : Math.min(limit, 20);
        return biliNftDetailMapper.selectRandomImagesByItemId(itemId, validLimit);
    }

    /**
     * 按多个系列ID随机查询每个系列指定数量的藏品图片。
     *
     * @param itemIds 系列ID列表
     * @param limit 每个系列的数量
     * @return 藏品图片列表
     */
    @Override
    public List<BiliNftImage> listRandomImagesByItemIds(List<Long> itemIds, Integer limit) {
        if (itemIds == null || itemIds.isEmpty()) {
            return List.of();
        }
        int validLimit = limit == null || limit < 1 ? 5 : Math.min(limit, 20);
        return biliNftDetailMapper.selectRandomImagesByItemIds(itemIds, validLimit);
    }

    /**
     * 分页查询藏品明细记录。
     *
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<BiliNftDetail> listPage(Integer pageNum, Integer pageSize) {
        int validPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int validPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int offset = (validPageNum - 1) * validPageSize;
        Long total = biliNftDetailMapper.countAll();
        List<BiliNftDetail> records = biliNftDetailMapper.selectPage(offset, validPageSize);
        return new PageResult<>(total, validPageNum, validPageSize, records);
    }
}
