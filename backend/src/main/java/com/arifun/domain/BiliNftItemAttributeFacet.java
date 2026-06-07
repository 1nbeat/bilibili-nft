package com.arifun.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiliNftItemAttributeFacet {

    private String traitType;
    private List<BiliNftItemAttributeOption> options;
}
