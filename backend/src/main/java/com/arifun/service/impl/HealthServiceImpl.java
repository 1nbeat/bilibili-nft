package com.arifun.service.impl;

import com.arifun.service.HealthService;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class HealthServiceImpl implements HealthService {

    /**
     * 组装基础健康检查结果。
     *
     * @return 健康状态信息
     */
    @Override
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("application", "bilibili-nft-backend");
        result.put("timestamp", LocalDateTime.now().toString());
        return result;
    }
}
