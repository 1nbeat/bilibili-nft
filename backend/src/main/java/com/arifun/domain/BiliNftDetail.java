package com.arifun.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiliNftDetail {

    private Long id;
    private Long itemId;
    private String itemName;
    private String issuerName;
    private Integer collectionTotal;
    private Boolean privateFlag;
    private String serialNumber;
    private Integer serialNo;
    private Integer serialTotal;
    private String tokenId;
    private String nftId;
    private Integer nftType;
    private Long mid;
    private String username;
    private String avatarUrl;
    private String imageUrl;
    private String animationUrl;
    private LocalDateTime crawlTime;
}
