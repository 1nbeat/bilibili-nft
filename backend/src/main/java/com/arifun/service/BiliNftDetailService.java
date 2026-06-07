package com.arifun.service;

import com.arifun.domain.BiliNftDetail;
import com.arifun.domain.BiliNftImage;
import com.arifun.domain.PageResult;
import java.util.List;

public interface BiliNftDetailService {

    /**
     * 新增一条藏品明细记录。
     *
     * @param detail 藏品明细
     */
    void create(BiliNftDetail detail);

    /**
     * 按唯一键新增或更新一条藏品明细记录。
     *
     * @param detail 藏品明细
     */
    void upsert(BiliNftDetail detail);

    /**
     * 按主键更新一条藏品明细记录。
     *
     * @param detail 藏品明细
     */
    void update(BiliNftDetail detail);

    /**
     * 按主键删除一条藏品明细记录。
     *
     * @param id 主键ID
     */
    void deleteById(Long id);

    /**
     * 按系列ID删除全部藏品明细记录。
     *
     * @param itemId 系列ID
     */
    void deleteByItemId(Long itemId);

    /**
     * 按主键查询一条藏品明细记录。
     *
     * @param id 主键ID
     * @return 藏品明细
     */
    BiliNftDetail getById(Long id);

    /**
     * 查询全部藏品明细记录。
     *
     * @return 藏品明细列表
     */
    List<BiliNftDetail> listAll();

    /**
     * 按系列ID分页查询藏品明细记录。
     *
     * @param itemId 系列ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<BiliNftDetail> listPageByItemId(Long itemId, Integer pageNum, Integer pageSize);

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
     * @return 藏品图片列表
     */
    List<BiliNftImage> listRandomImagesByItemIds(List<Long> itemIds, Integer limit);

    /**
     * 分页查询藏品明细记录。
     *
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<BiliNftDetail> listPage(Integer pageNum, Integer pageSize);
}
