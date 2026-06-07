package com.arifun.spider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BiliBiliNftAttributeDto {

    @JsonProperty("trait_type")
    private String traitType;

    private String value;

    @JsonProperty("trait_count")
    private Integer traitCount;
}

