package com.arifun.spider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BiliBiliNftDetailIssueDto {

    private String author;
    private String issuer;

    @JsonProperty("issue_time")
    private String issueTime;

    @JsonProperty("issue_number")
    private Integer issueNumber;
}

