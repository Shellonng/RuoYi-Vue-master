package com.ruoyi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI配置类
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "ai")
public class AIConfig
{
    /** 阿里云DashScope API Key */
    private String apiKey;

    /** AI模型名称 */
    private String model = "qwen-plus";

    /** 请求超时时间（秒） */
    private Integer timeout = 60;

    /** 是否启用AI功能 */
    private Boolean enabled = true;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
