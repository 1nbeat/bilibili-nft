package com.arifun.service;

import java.util.Map;

public interface HealthService {

    /**
     * 构建后端服务健康状态信息。
     *
     * @return 健康状态信息
     */
    Map<String, Object> health();
}
