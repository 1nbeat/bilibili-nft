package com.arifun.controller;

import com.arifun.service.HealthService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    /**
     * 获取后端服务当前健康状态。
     *
     * @return 健康状态信息
     */
    @GetMapping
    public Map<String, Object> health() {
        return healthService.health();
    }
}
