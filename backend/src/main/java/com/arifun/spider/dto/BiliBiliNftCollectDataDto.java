package com.arifun.spider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class BiliBiliNftCollectDataDto {

    @JsonProperty("item_id")
    private Long itemId;

    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("issuer_name")
    private String issuerName;

    @JsonProperty("nft_list")
    private List<BiliBiliNftCollectItemDto> nftList;

    private Integer total;

    @JsonProperty("private")
    private Boolean privateFlag;
}

