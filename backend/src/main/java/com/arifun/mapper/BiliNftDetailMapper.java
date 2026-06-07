package com.arifun.mapper;

import com.arifun.domain.BiliNftDetail;
import com.arifun.domain.BiliNftImage;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BiliNftDetailMapper {

    /**
     * 新增一条藏品明细记录。
     *
     * @param detail 藏品明细
     * @return 影响行数
     */
    int insert(BiliNftDetail detail);

    /**
     * 按唯一键新增或更新一条藏品明细记录。
     *
     * @param detail 藏品明细
     * @return 影响行数
     */
    int upsert(BiliNftDetail detail);

    /**
     * 按主键更新一条藏品明细记录。
     *
     * @param detail 藏品明细
     * @return 影响行数
     */
    int updateById(BiliNftDetail detail);

    /**
     * 按主键删除一条藏品明细记录。
     *
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 按系列ID删除全部藏品明细记录。
     *
     * @param itemId 系列ID
     * @return 影响行数
     */
    int deleteByItemId(@Param("itemId") Long itemId);

    /**
     * 按主键查询一条藏品明细记录。
     *
     * @param id 主键ID
     * @return 藏品明细
     */
    BiliNftDetail selectById(@Param("id") Long id);

    /**
     * 查询全部藏品明细记录。
     *
     * @return 藏品明细列表
     */
    List<BiliNftDetail> selectAll();

    /**
     * 按系列ID分页查询藏品明细记录。
     *
     * @param itemId 系列ID
     * @param offset 起始偏移量
     * @param limit 分页大小
     * @return 藏品明细列表
     */
    List<BiliNftDetail> selectPageByItemId(
            @Param("itemId") Long itemId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 按系列ID统计藏品明细总数。
     *
     * @param itemId 系列ID
     * @return 藏品明细总数
     */
    Long countByItemId(@Param("itemId") Long itemId);

    /**
     * 按系列ID随机查询指定数量的藏品图片。
     *
     * @param itemId 系列ID
     * @param limit 数量
     * @return 藏品图片列表
     */
    List<BiliNftImage> selectRandomImagesByItemId(
            @Param("itemId") Long itemId,
            @Param("limit") Integer limit);

    /**
     * 按多个系列ID随机查询每个系列指定数量的藏品图片。
     *
     * @param itemIds 系列ID列表
     * @param limit 每个系列的数量
     * @return 藏品图片列表
     */
    List<BiliNftImage> selectRandomImagesByItemIds(
            @Param("itemIds") List<Long> itemIds,
            @Param("limit") Integer limit);

    /**
     * 分页查询藏品明细记录。
     *
     * @param offset 起始偏移量
     * @param limit 分页大小
     * @return 藏品明细列表
     */
    List<BiliNftDetail> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 统计藏品明细总数。
     *
     * @return 藏品明细总数
     */
    Long countAll();
}
