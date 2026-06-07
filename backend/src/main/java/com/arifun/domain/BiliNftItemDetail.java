package com.arifun.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiliNftItemDetail {

    private Long id;
    private Long itemId;
    private String nftId;
    private String nftName;
    private String serialNumber;
    private Integer serialNo;
    private Integer serialTotal;
    private String nftAddress;
    private String imageUrl;
    private String animationUrl;
    private String htmlUrl;
    private String modelText;
    private Long ownerMid;
    private String ownerName;
    private String ownerAvatarUrl;
    private String ownerTxHash;
    private LocalDate ownerTxTime;
    private String ownerExpireTime;
    private String issueAuthor;
    private String issueIssuer;
    private LocalDate issueTime;
    private Integer issueNumber;
    private String description;
    private String attributesJson;
    private LocalDateTime crawlTime;
}

