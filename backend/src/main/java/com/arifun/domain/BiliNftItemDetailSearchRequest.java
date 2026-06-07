package com.arifun.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiliNftItemDetailSearchRequest {

    private Long itemId;
    private Integer pageNum;
    private Integer pageSize;
    private List<BiliNftAttributeFilter> filters;
}
