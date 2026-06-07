package com.arifun.controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final HttpClient httpClient;

    public MediaController() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 代理远程图片，避免浏览器直接请求第三方图片地址时触发防盗链。
     *
     * @param url 远程图片地址
     * @return 图片二进制响应
     * @throws IOException IO异常
     * @throws InterruptedException 中断异常
     */
    @GetMapping("/image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) throws IOException, InterruptedException {
        if (!StringUtils.hasText(url)) {
            return ResponseEntity.badRequest().build();
        }

        String normalizedUrl = normalizeUrl(url);
        if (!hasHttpScheme(normalizedUrl)) {
            return ResponseEntity.badRequest().build();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(normalizedUrl))
                .timeout(Duration.ofSeconds(20))
                .header(HttpHeaders.ACCEPT, "*/*")
                .header(HttpHeaders.USER_AGENT, "Mozilla/5.0")
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return ResponseEntity.status(response.statusCode()).build();
        }

        MediaType mediaType = resolveMediaType(response.headers().firstValue(HttpHeaders.CONTENT_TYPE).orElse(null));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentType(mediaType)
                .body(response.body());
    }

    /**
     * 统一修正图片地址协议。
     *
     * @param url 原始地址
     * @return 规范化后的地址
     */
    private String normalizeUrl(String url) {
        if (url.startsWith("http://")) {
            return "https://" + url.substring("http://".length());
        }
        return url;
    }

    /**
     * 判断地址是否带有 http 或 https 协议。
     *
     * @param url 原始地址
     * @return 是否是合法的 http 地址
     */
    private boolean hasHttpScheme(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    /**
     * 解析响应头中的媒体类型。
     *
     * @param contentType 响应头内容类型
     * @return Spring媒体类型
     */
    private MediaType resolveMediaType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.IMAGE_JPEG;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception e) {
            List<MediaType> fallbackTypes = List.of(
                    MediaType.IMAGE_JPEG,
                    MediaType.IMAGE_PNG,
                    MediaType.IMAGE_GIF,
                    MediaType.APPLICATION_OCTET_STREAM
            );
            return fallbackTypes.getFirst();
        }
    }
}
