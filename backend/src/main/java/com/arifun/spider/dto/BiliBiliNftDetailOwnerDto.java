package com.arifun.spider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BiliBiliNftDetailOwnerDto {

    private Long mid;
    private String name;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("tx_hash")
    private String txHash;

    @JsonProperty("tx_time")
    private String txTime;

    @JsonProperty("expire_time")
    private String expireTime;
}

