package com.arifun.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiliNftItemAttribute {

    private Long id;
    private Long itemId;
    private String nftId;
    private String traitType;
    private String traitValue;
    private Integer traitCount;
    private BigDecimal rarityRate;
    private LocalDateTime crawlTime;
}
