package com.arifun.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiliNftImage {

    private Long itemId;
    private String nftId;
    private String imageUrl;
}
