package com.arifun.spider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class BiliBiliNftDetailDataDto {

    @JsonProperty("item_id")
    private Long itemId;

    @JsonProperty("id")
    private String nftId;

    @JsonProperty("name")
    private String nftName;

    @JsonProperty("serial_number")
    private String serialNumber;

    @JsonProperty("nft_address")
    private String nftAddress;

    @JsonProperty("image")
    private String imageUrl;

    @JsonProperty("animation_url")
    private String animationUrl;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("model")
    private String modelText;

    private BiliBiliNftDetailOwnerDto owner;
    private BiliBiliNftDetailIssueDto issue;
    private List<BiliBiliNftAttributeDto> attributes;
    private String description;
}

