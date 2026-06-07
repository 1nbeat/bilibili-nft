package com.arifun.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiliNftAttributeFilter {

    private String traitType;
    private String traitValue;
}
