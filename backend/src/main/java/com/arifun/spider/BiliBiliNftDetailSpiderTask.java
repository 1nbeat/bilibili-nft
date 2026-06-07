package com.arifun.spider;

import com.arifun.domain.BiliNftItemAttribute;
import com.arifun.domain.BiliNftItemDetail;
import com.arifun.service.BiliNftItemAttributeService;
import com.arifun.service.BiliNftItemDetailService;
import com.arifun.spider.dto.BiliBiliNftAttributeDto;
import com.arifun.spider.dto.BiliBiliNftDetailDataDto;
import com.arifun.spider.dto.BiliBiliNftDetailIssueDto;
import com.arifun.spider.dto.BiliBiliNftDetailOwnerDto;
import com.arifun.spider.dto.BiliBiliNftDetailResponseDto;
import com.arifun.spider.dto.BiliBiliNftDetailSpiderResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BiliBiliNftDetailSpiderTask {

    private static final String API_URL = "https://baselabs.bilibili.com/x/gallery/nft_detail";
    private static final int SUCCESS_CODE = 0;
    private static final int NETWORK_RETRY_COUNT = 3;
    private static final long NETWORK_RETRY_BASE_DELAY_MILLIS = 300;
    private static final int DATABASE_RETRY_COUNT = 3;
    private static final long DATABASE_RETRY_BASE_DELAY_MILLIS = 150;

    private final BiliNftItemDetailService biliNftItemDetailService;
    private final BiliNftItemAttributeService biliNftItemAttributeService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public BiliBiliNftDetailSpiderTask(
            BiliNftItemDetailService biliNftItemDetailService,
            BiliNftItemAttributeService biliNftItemAttributeService,
            ObjectMapper objectMapper) {
        this.biliNftItemDetailService = biliNftItemDetailService;
        this.biliNftItemAttributeService = biliNftItemAttributeService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    /**
     * 抓取指定藏品详情并入库。
     *
     * @param nftId 藏品ID
     * @return 抓取结果
     */
    public BiliBiliNftDetailSpiderResult crawl(String nftId) {
        BiliBiliNftDetailResponseDto response = fetchDetail(nftId);

        if (response == null) {
            log.warn("抓取详情响应为空，nftId={}", nftId);
            return new BiliBiliNftDetailSpiderResult(nftId, null, false, "empty response");
        }

        if (response.getCode() == null || response.getCode() != SUCCESS_CODE || response.getData() == null) {
            log.error("抓取详情失败，nftId={}, code={}, message={}", nftId, response.getCode(), response.getMessage());
            throw new IllegalStateException("Detail spider request failed, code=" + response.getCode() + ", message=" + response.getMessage());
        }

        BiliBiliNftDetailDataDto data = response.getData();
        BiliNftItemDetail detail = toEntity(data);
        biliNftItemDetailService.upsert(detail);
        rebuildAttributesWithRetry(detail.getNftId(), toAttributeEntities(data, detail.getCrawlTime()));
        return new BiliBiliNftDetailSpiderResult(data.getNftId(), data.getItemId(), true, "success");
    }

    /**
     * MySQL deadlock is a transient write conflict under concurrent crawling, so retry the attribute rebuild.
     *
     * @param nftId 藏品ID
     * @param attributes 属性明细实体列表
     */
    private void rebuildAttributesWithRetry(String nftId, List<BiliNftItemAttribute> attributes) {
        for (int attempt = 1; attempt <= DATABASE_RETRY_COUNT; attempt++) {
            try {
                biliNftItemAttributeService.rebuildByNftId(nftId, attributes);
                return;
            } catch (TransientDataAccessException e) {
                if (attempt >= DATABASE_RETRY_COUNT) {
                    throw e;
                }
                long delayMillis = DATABASE_RETRY_BASE_DELAY_MILLIS * attempt
                        + ThreadLocalRandom.current().nextLong(100);
                log.warn("属性入库遇到临时数据库冲突，准备重试，nftId={}, attempt={}, delayMillis={}, error={}",
                        nftId, attempt, delayMillis, e.getMessage());
                sleepBeforeRetry(delayMillis);
            }
        }
    }

    private void sleepBeforeRetry(long delayMillis) {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Database retry interrupted", e);
        }
    }

    /**
     * 请求指定藏品详情数据。
     *
     * @param nftId 藏品ID
     * @return 接口响应
     */
    private BiliBiliNftDetailResponseDto fetchDetail(String nftId) {
        String url = API_URL + "?id=" + URLEncoder.encode(nftId, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        for (int attempt = 1; attempt <= NETWORK_RETRY_COUNT; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                return objectMapper.readValue(response.body(), BiliBiliNftDetailResponseDto.class);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Failed to fetch BiliBili NFT detail data", e);
            } catch (IOException e) {
                if (attempt >= NETWORK_RETRY_COUNT) {
                    log.error("请求藏品详情接口异常，nftId={}, attempt={}", nftId, attempt, e);
                    throw new IllegalStateException("Failed to fetch BiliBili NFT detail data", e);
                }
                long delayMillis = NETWORK_RETRY_BASE_DELAY_MILLIS * attempt
                        + ThreadLocalRandom.current().nextLong(200);
                log.warn("请求藏品详情接口遇到临时网络异常，准备重试，nftId={}, attempt={}, delayMillis={}",
                        nftId, attempt, delayMillis, e);
                sleepBeforeRetry(delayMillis);
            }
        }

        throw new IllegalStateException("Failed to fetch BiliBili NFT detail data");
    }

    /**
     * 将详情接口数据转换为数据库实体。
     *
     * @param data 详情数据
     * @return 数据库实体
     */
    private BiliNftItemDetail toEntity(BiliBiliNftDetailDataDto data) {
        BiliNftItemDetail detail = new BiliNftItemDetail();
        detail.setItemId(data.getItemId());
        detail.setNftId(data.getNftId());
        detail.setNftName(data.getNftName());
        detail.setSerialNumber(data.getSerialNumber());
        detail.setSerialNo(parseSerialNo(data.getSerialNumber()));
        detail.setSerialTotal(parseSerialTotal(data.getSerialNumber()));
        detail.setNftAddress(data.getNftAddress());
        detail.setImageUrl(data.getImageUrl());
        detail.setAnimationUrl(data.getAnimationUrl());
        detail.setHtmlUrl(data.getHtmlUrl());
        detail.setModelText(data.getModelText());
        fillOwner(detail, data.getOwner());
        fillIssue(detail, data.getIssue());
        detail.setDescription(data.getDescription());
        detail.setAttributesJson(toJson(data.getAttributes()));
        detail.setCrawlTime(LocalDateTime.now());
        return detail;
    }

    /**
     * 将属性列表转换为属性明细实体。
     *
     * @param data 详情数据
     * @param crawlTime 爬取时间
     * @return 属性明细实体列表
     */
    private List<BiliNftItemAttribute> toAttributeEntities(BiliBiliNftDetailDataDto data, LocalDateTime crawlTime) {
        List<BiliNftItemAttribute> attributes = new ArrayList<>();
        if (data.getAttributes() == null || data.getAttributes().isEmpty()) {
            return attributes;
        }

        Integer serialTotal = parseSerialTotal(data.getSerialNumber());
        for (BiliBiliNftAttributeDto attribute : data.getAttributes()) {
            if (attribute == null
                    || attribute.getTraitType() == null
                    || attribute.getTraitType().isBlank()
                    || attribute.getValue() == null
                    || attribute.getValue().isBlank()) {
                continue;
            }

            BiliNftItemAttribute entity = new BiliNftItemAttribute();
            entity.setItemId(data.getItemId());
            entity.setNftId(data.getNftId());
            entity.setTraitType(attribute.getTraitType().trim());
            entity.setTraitValue(attribute.getValue().trim());
            entity.setTraitCount(attribute.getTraitCount());
            entity.setRarityRate(calculateRarityRate(attribute.getTraitCount(), serialTotal));
            entity.setCrawlTime(crawlTime);
            attributes.add(entity);
        }

        return attributes;
    }

    /**
     * 填充持有人信息。
     *
     * @param detail 数据库实体
     * @param owner 持有人信息
     */
    private void fillOwner(BiliNftItemDetail detail, BiliBiliNftDetailOwnerDto owner) {
        if (owner == null) {
            return;
        }
        detail.setOwnerMid(owner.getMid());
        detail.setOwnerName(owner.getName());
        detail.setOwnerAvatarUrl(owner.getAvatarUrl());
        detail.setOwnerTxHash(owner.getTxHash());
        detail.setOwnerTxTime(parseDate(owner.getTxTime()));
        detail.setOwnerExpireTime(owner.getExpireTime());
    }

    /**
     * 填充发行信息。
     *
     * @param detail 数据库实体
     * @param issue 发行信息
     */
    private void fillIssue(BiliNftItemDetail detail, BiliBiliNftDetailIssueDto issue) {
        if (issue == null) {
            return;
        }
        detail.setIssueAuthor(issue.getAuthor());
        detail.setIssueIssuer(issue.getIssuer());
        detail.setIssueTime(parseDate(issue.getIssueTime()));
        detail.setIssueNumber(issue.getIssueNumber());
    }

    /**
     * 将属性列表转为 JSON 字符串。
     *
     * @param attributes 属性列表
     * @return JSON 字符串
     */
    private String toJson(List<BiliBiliNftAttributeDto> attributes) {
        if (attributes == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize attributes", e);
        }
    }

    /**
     * 计算稀有度。
     *
     * @param traitCount 出现次数
     * @param serialTotal 系列总数
     * @return 稀有度
     */
    private BigDecimal calculateRarityRate(Integer traitCount, Integer serialTotal) {
        if (traitCount == null || serialTotal == null || serialTotal <= 0) {
            return null;
        }
        return BigDecimal.valueOf(traitCount)
                .divide(BigDecimal.valueOf(serialTotal), 6, RoundingMode.HALF_UP);
    }

    /**
     * 从展示编号中提取当前序号。
     *
     * @param serialNumber 展示编号
     * @return 当前序号
     */
    private Integer parseSerialNo(String serialNumber) {
        if (serialNumber == null || !serialNumber.contains("/")) {
            return null;
        }
        String[] parts = serialNumber.replace("#", "").split("/");
        if (parts.length != 2) {
            return null;
        }
        return parseInteger(parts[0]);
    }

    /**
     * 从展示编号中提取总量。
     *
     * @param serialNumber 展示编号
     * @return 总量
     */
    private Integer parseSerialTotal(String serialNumber) {
        if (serialNumber == null || !serialNumber.contains("/")) {
            return null;
        }
        String[] parts = serialNumber.replace("#", "").split("/");
        if (parts.length != 2) {
            return null;
        }
        return parseInteger(parts[1]);
    }

    /**
     * 安全解析整数。
     *
     * @param value 字符串值
     * @return 整数结果
     */
    private Integer parseInteger(String value) {
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 安全解析日期。
     *
     * @param value 日期字符串
     * @return 日期结果
     */
    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
