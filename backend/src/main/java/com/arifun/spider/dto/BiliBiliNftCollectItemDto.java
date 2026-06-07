package com.arifun.spider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BiliBiliNftCollectItemDto {

    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("serial_number")
    private String serialNumber;

    @JsonProperty("token_id")
    private String tokenId;

    private Long mid;

    private String username;

    @JsonProperty("avatar")
    private String avatarUrl;

    @JsonProperty("image")
    private String imageUrl;

    @JsonProperty("animation_url")
    private String animationUrl;

    @JsonProperty("nft_type")
    private Integer nftType;

    @JsonProperty("nft_id")
    private String nftId;
}

