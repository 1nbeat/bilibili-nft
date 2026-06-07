package com.arifun.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiliNftSeries {

    private Long id;
    private Long itemId;
    private String itemName;
    private String issuerName;
    private Integer collectionTotal;
    private Boolean privateFlag;
    private LocalDateTime crawlTime;
}

