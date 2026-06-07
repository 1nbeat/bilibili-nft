package com.arifun.mapper;

import com.arifun.domain.BiliNftItemDetail;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BiliNftItemDetailMapper {

    /**
     * 按唯一键新增或更新一条藏品详情记录。
     *
     * @param detail 藏品详情
     * @return 影响行数
     */
    int upsert(BiliNftItemDetail detail);

    /**
     * 按藏品ID查询一条藏品详情记录。
     *
     * @param nftId 藏品ID
     * @return 藏品详情
     */
    BiliNftItemDetail selectByNftId(@Param("nftId") String nftId);

    /**
     * 按藏品ID列表查询详情。
     *
     * @param nftIds 藏品ID列表
     * @return 藏品详情列表
     */
    List<BiliNftItemDetail> selectByNftIds(@Param("nftIds") List<String> nftIds);
}
