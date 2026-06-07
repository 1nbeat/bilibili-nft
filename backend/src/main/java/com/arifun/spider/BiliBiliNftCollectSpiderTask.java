package com.arifun.spider;

import com.arifun.domain.BiliNftDetail;
import com.arifun.domain.BiliNftSeries;
import com.arifun.service.BiliNftDetailService;
import com.arifun.service.BiliNftSeriesService;
import com.arifun.spider.dto.BiliBiliNftBatchSpiderResult;
import com.arifun.spider.dto.BiliBiliNftCollectDataDto;
import com.arifun.spider.dto.BiliBiliNftCollectItemDto;
import com.arifun.spider.dto.BiliBiliNftCollectResponseDto;
import com.arifun.spider.dto.BiliBiliNftDetailSpiderResult;
import com.arifun.spider.dto.BiliBiliNftSpiderResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BiliBiliNftCollectSpiderTask {

    private static final String API_URL = "https://baselabs.bilibili.com/x/gallery/nft/collect/list";
    private static final int SUCCESS_CODE = 0;
    private static final int EMPTY_CODE = 12003003;
    private static final int BATCH_THREAD_COUNT = 5;
    private static final int DETAIL_THREAD_COUNT = 5;
    private static final int NETWORK_RETRY_COUNT = 3;
    private static final long NETWORK_RETRY_BASE_DELAY_MILLIS = 300;

    private final BiliNftDetailService biliNftDetailService;
    private final BiliNftSeriesService biliNftSeriesService;
    private final BiliBiliNftDetailSpiderTask biliBiliNftDetailSpiderTask;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ExecutorService detailExecutorService;

    public BiliBiliNftCollectSpiderTask(
            BiliNftDetailService biliNftDetailService,
            BiliNftSeriesService biliNftSeriesService,
            BiliBiliNftDetailSpiderTask biliBiliNftDetailSpiderTask,
            ObjectMapper objectMapper) {
        this.biliNftDetailService = biliNftDetailService;
        this.biliNftSeriesService = biliNftSeriesService;
        this.biliBiliNftDetailSpiderTask = biliBiliNftDetailSpiderTask;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.detailExecutorService = Executors.newFixedThreadPool(DETAIL_THREAD_COUNT);
    }

    /**
     * 使用默认分页大小抓取指定系列数据。
     *
     * @param itemId 系列ID
     * @return 抓取结果
     */
    public BiliBiliNftSpiderResult crawl(Long itemId) {
        return crawl(itemId, 10);
    }

    /**
     * 按分页遍历抓取指定系列数据，并联动抓取每个藏品的详情。
     *
     * @param itemId 系列ID
     * @param pageSize 每页条数
     * @return 抓取结果
     */
    public BiliBiliNftSpiderResult crawl(Long itemId, Integer pageSize) {
        int pageNo = 1;
        int crawledPages = 0;
        int savedCount = 0;
        String stopReason = "crawl finished";

        log.info("开始抓取B站数字藏品列表，itemId={}, pageSize={}", itemId, pageSize);
        biliNftDetailService.deleteByItemId(itemId);
        log.info("已删除该系列原有藏品列表数据，itemId={}", itemId);

        while (true) {
            BiliBiliNftCollectResponseDto response = fetchPage(itemId, pageNo, pageSize);

            if (response == null) {
                stopReason = "empty response";
                log.warn("列表抓取响应为空，itemId={}, pageNo={}", itemId, pageNo);
                break;
            }

            if (response.getCode() != null && response.getCode() == EMPTY_CODE) {
                stopReason = response.getMessage();
                log.info("列表抓取结束，接口返回无数据，itemId={}, pageNo={}, message={}",
                        itemId, pageNo, response.getMessage());
                break;
            }

            if (response.getCode() == null || response.getCode() != SUCCESS_CODE || response.getData() == null) {
                log.error("列表抓取失败，itemId={}, pageNo={}, code={}, message={}",
                        itemId, pageNo, response.getCode(), response.getMessage());
                throw new IllegalStateException(
                        "Spider request failed, code=" + response.getCode() + ", message=" + response.getMessage());
            }

            BiliBiliNftCollectDataDto data = response.getData();
            biliNftSeriesService.saveIfAbsent(toSeriesEntity(data));

            List<BiliBiliNftCollectItemDto> nftList = data.getNftList();
            if (nftList == null || nftList.isEmpty()) {
                stopReason = "empty nft list";
                log.info("列表抓取结束，当前页无藏品数据，itemId={}, pageNo={}", itemId, pageNo);
                break;
            }

            log.info("抓取到一页列表数据，itemId={}, pageNo={}, pageSize={}, currentCount={}, total={}",
                    itemId, pageNo, pageSize, nftList.size(), data.getTotal());

            List<Future<?>> detailTasks = new ArrayList<>();
            for (BiliBiliNftCollectItemDto item : nftList) {
                biliNftDetailService.upsert(toEntity(data, item));
                Future<?> detailTask = submitDetailCrawl(item.getNftId());
                if (detailTask != null) {
                    detailTasks.add(detailTask);
                }
                savedCount++;
            }
            waitForDetailTasks(itemId, pageNo, detailTasks);

            log.info("当前页列表入库完成，itemId={}, pageNo={}, savedCount={}", itemId, pageNo, savedCount);

            crawledPages++;

            if (nftList.size() < pageSize) {
                stopReason = "last page reached";
                log.info("列表抓取结束，已到最后一页，itemId={}, pageNo={}, currentCount={}",
                        itemId, pageNo, nftList.size());
                break;
            }

            pageNo++;
        }

        log.info("列表抓取任务完成，itemId={}, crawledPages={}, savedCount={}, stopReason={}",
                itemId, crawledPages, savedCount, stopReason);
        return new BiliBiliNftSpiderResult(itemId, pageSize, crawledPages, savedCount, stopReason);
    }

    /**
     * 提交单个藏品详情抓取任务。
     *
     * @param nftId 藏品ID
     * @return 异步任务句柄
     */
    private Future<?> submitDetailCrawl(String nftId) {
        if (nftId == null || nftId.isBlank()) {
            return null;
        }

        return detailExecutorService.submit(() -> crawlDetailSafely(nftId));
    }

    /**
     * 等待当前页详情抓取任务完成。
     *
     * @param itemId 系列ID
     * @param pageNo 页码
     * @param detailTasks 详情抓取任务
     */
    private void waitForDetailTasks(Long itemId, Integer pageNo, List<Future<?>> detailTasks) {
        for (Future<?> detailTask : detailTasks) {
            try {
                detailTask.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(
                        "Detail crawl interrupted, itemId=" + itemId + ", pageNo=" + pageNo, e);
            } catch (Exception e) {
                log.warn("等待详情抓取任务完成时出现异常，itemId={}, pageNo={}", itemId, pageNo, e);
            }
        }
    }

    /**
     * 安全抓取单个藏品详情，避免单条详情失败中断整个列表任务。
     *
     * @param nftId 藏品ID
     */
    private void crawlDetailSafely(String nftId) {

        try {
            BiliBiliNftDetailSpiderResult result = biliBiliNftDetailSpiderTask.crawl(nftId);
        } catch (Exception e) {
            log.warn("列表联动详情抓取失败，nftId={}", nftId, e);
        }
    }

    /**
     * 按系列ID范围使用5个线程执行批量抓取任务。
     *
     * @param startItemId 起始系列ID
     * @param endItemId 结束系列ID
     * @param pageSize 每页条数
     * @return 批量抓取结果
     */
    public BiliBiliNftBatchSpiderResult batchCrawl(Long startItemId, Long endItemId, Integer pageSize) {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);
        AtomicLong lastProcessedItemId = new AtomicLong(startItemId - 1);
        List<Long> failedItemIds = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executorService = Executors.newFixedThreadPool(BATCH_THREAD_COUNT);

        log.info("开始执行批量抓取任务，startItemId={}, endItemId={}, pageSize={}, threadCount={}",
                startItemId, endItemId, pageSize, BATCH_THREAD_COUNT);

        try {
            for (long currentItemId = startItemId; currentItemId <= endItemId; currentItemId++) {
                final long taskItemId = currentItemId;
                executorService.submit(() -> {
                    lastProcessedItemId.accumulateAndGet(taskItemId, Math::max);
                    try {
//                        log.info("开始抓取系列，itemId={}", taskItemId);
                        crawl(taskItemId, pageSize);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failedCount.incrementAndGet();
                        failedItemIds.add(taskItemId);
                        log.error("批量抓取单个系列失败，itemId={}", taskItemId, e);
                    }
                });
            }
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(7, TimeUnit.DAYS)) {
                    log.warn("批量抓取线程池等待超时，startItemId={}, endItemId={}", startItemId, endItemId);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Batch crawl interrupted", e);
            }
        }

        log.info("批量抓取任务结束，startItemId={}, endItemId={}, successCount={}, failedCount={}, lastProcessedItemId={}",
                startItemId, endItemId, successCount.get(), failedCount.get(), lastProcessedItemId.get());

        return new BiliBiliNftBatchSpiderResult(
                startItemId,
                endItemId,
                pageSize,
                successCount.get(),
                failedCount.get(),
                lastProcessedItemId.get(),
                new ArrayList<>(failedItemIds));
    }

    /**
     * 请求指定页码的数据。
     *
     * @param itemId 系列ID
     * @param pageNo 页码
     * @param pageSize 每页条数
     * @return 接口响应
     */
    private BiliBiliNftCollectResponseDto fetchPage(Long itemId, Integer pageNo, Integer pageSize) {
        String url = API_URL
                + "?ps=" + pageSize
                + "&pn=" + pageNo
                + "&item_id=" + URLEncoder.encode(String.valueOf(itemId), StandardCharsets.UTF_8);

        log.debug("请求列表抓取接口，itemId={}, pageNo={}, pageSize={}, url={}", itemId, pageNo, pageSize, url);

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
                return objectMapper.readValue(response.body(), BiliBiliNftCollectResponseDto.class);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Failed to fetch BiliBili NFT data", e);
            } catch (IOException e) {
                if (attempt >= NETWORK_RETRY_COUNT) {
                    log.error("请求列表抓取接口异常，itemId={}, pageNo={}, pageSize={}, attempt={}",
                            itemId, pageNo, pageSize, attempt, e);
                    throw new IllegalStateException("Failed to fetch BiliBili NFT data", e);
                }
                long delayMillis = NETWORK_RETRY_BASE_DELAY_MILLIS * attempt
                        + ThreadLocalRandom.current().nextLong(200);
                log.warn("请求列表抓取接口遇到临时网络异常，准备重试，itemId={}, pageNo={}, attempt={}, delayMillis={}",
                        itemId, pageNo, attempt, delayMillis, e);
                sleepBeforeRetry(delayMillis);
            }
        }

        throw new IllegalStateException("Failed to fetch BiliBili NFT data");
    }

    private void sleepBeforeRetry(long delayMillis) {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Network retry interrupted", e);
        }
    }

    /**
     * 将列表接口单条藏品数据转换为数据库实体。
     *
     * @param data 系列数据
     * @param item 单条藏品数据
     * @return 数据库实体
     */
    private BiliNftDetail toEntity(BiliBiliNftCollectDataDto data, BiliBiliNftCollectItemDto item) {
        BiliNftDetail detail = new BiliNftDetail();
        detail.setItemId(data.getItemId());
        detail.setItemName(defaultString(item.getItemName(), data.getItemName()));
        detail.setIssuerName(data.getIssuerName());
        detail.setCollectionTotal(data.getTotal());
        detail.setPrivateFlag(Boolean.TRUE.equals(data.getPrivateFlag()));
        detail.setSerialNumber(item.getSerialNumber());
        detail.setSerialNo(parseSerialNo(item.getSerialNumber()));
        detail.setSerialTotal(parseSerialTotal(item.getSerialNumber()));
        detail.setTokenId(item.getTokenId());
        detail.setNftId(item.getNftId());
        detail.setNftType(item.getNftType());
        detail.setMid(item.getMid());
        detail.setUsername(item.getUsername());
        detail.setAvatarUrl(item.getAvatarUrl());
        detail.setImageUrl(item.getImageUrl());
        detail.setAnimationUrl(item.getAnimationUrl());
        detail.setCrawlTime(LocalDateTime.now());
        return detail;
    }

    /**
     * 将系列数据转换为数据库实体。
     *
     * @param data 系列数据
     * @return 系列实体
     */
    private BiliNftSeries toSeriesEntity(BiliBiliNftCollectDataDto data) {
        BiliNftSeries series = new BiliNftSeries();
        series.setItemId(data.getItemId());
        series.setItemName(data.getItemName());
        series.setIssuerName(data.getIssuerName());
        series.setCollectionTotal(data.getTotal());
        series.setPrivateFlag(Boolean.TRUE.equals(data.getPrivateFlag()));
        series.setCrawlTime(LocalDateTime.now());
        return series;
    }

    /**
     * 优先返回首选字符串，为空时返回兜底值。
     *
     * @param preferred 首选值
     * @param fallback 兜底值
     * @return 最终字符串
     */
    private String defaultString(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        return fallback;
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
     * 关闭详情抓取线程池。
     */
    @PreDestroy
    public void shutdownDetailExecutorService() {
        detailExecutorService.shutdown();
        try {
            if (!detailExecutorService.awaitTermination(30, TimeUnit.SECONDS)) {
                detailExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            detailExecutorService.shutdownNow();
        }
    }
}
