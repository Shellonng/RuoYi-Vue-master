package com.ruoyi.common.utils.ai;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.concurrent.TimeUnit;

@Component
public class WhisperClientRenwu3
{
    private static final Logger log = LoggerFactory.getLogger(WhisperClientRenwu3.class);
    
    // Whisper API 配置（从配置文件读取）
    @Value("${whisper.api.url:http://localhost:8000/v1/audio/transcriptions}")
    private String whisperApiUrlConfig;
    
    @Value("${whisper.api.key:}")
    private String whisperApiKeyConfig;
    
    // 静态变量，用于静态方法访问
    private static String WHISPER_API_URL;
    private static String WHISPER_API_KEY;
    
    @PostConstruct
    private void init()
    {
        WHISPER_API_URL = whisperApiUrlConfig;
        WHISPER_API_KEY = whisperApiKeyConfig;
        log.info("【任务3-Whisper】配置初始化完成 - API URL: {}", WHISPER_API_URL);
    }
    
    private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS) // 语音识别可能需要较长时间
        .writeTimeout(60, TimeUnit.SECONDS)
        .build();
    
    public static String transcribeAudio(File audioFile) throws Exception
    {
        if (!audioFile.exists())
        {
            throw new IllegalArgumentException("音频文件不存在: " + audioFile.getAbsolutePath());
        }
        
        log.info("【任务3-Whisper】开始转录音频: {}, 大小: {} MB", 
            audioFile.getName(), audioFile.length() / 1024.0 / 1024.0);
        
        try
        {
            // 构建 multipart/form-data 请求
            // 注意：使用 OkHttp 3.x 兼容的 API
            MediaType mediaType = MediaType.parse("audio/wav");
            RequestBody fileBody = RequestBody.create(mediaType, audioFile);
            
            RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.getName(), fileBody)
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("language", "zh") // 指定中文，提高准确率
                .addFormDataPart("response_format", "text") // 直接返回文本
                .build();
            
            Request.Builder requestBuilder = new Request.Builder()
                .url(WHISPER_API_URL)
                .post(requestBody);
            
            // 如果有 API Key，添加到请求头
            if (WHISPER_API_KEY != null && !WHISPER_API_KEY.isEmpty())
            {
                requestBuilder.addHeader("Authorization", "Bearer " + WHISPER_API_KEY);
            }
            
            Request request = requestBuilder.build();
            
            log.info("【任务3-Whisper】调用 Whisper API: {}", WHISPER_API_URL);
            
            long startTime = System.currentTimeMillis();
            
            try (Response response = client.newCall(request).execute())
            {
                long duration = System.currentTimeMillis() - startTime;
                
                if (!response.isSuccessful())
                {
                    String errorBody = response.body() != null ? response.body().string() : "未知错误";
                    log.error("【任务3-Whisper】API 调用失败: {} - {}", response.code(), errorBody);
                    throw new RuntimeException("Whisper API 调用失败: " + response.code() + " - " + errorBody);
                }
                
                String transcription = response.body().string();
                
                log.info("【任务3-Whisper】转录成功，耗时: {}秒, 文本长度: {}", 
                    duration / 1000.0, transcription.length());
                log.debug("【任务3-Whisper】转录文本预览: {}", 
                    transcription.length() > 200 ? transcription.substring(0, 200) + "..." : transcription);
                
                return transcription.trim();
            }
        }
        catch (Exception e)
        {
            log.error("【任务3-Whisper】音频转录失败", e);
            throw new RuntimeException("音频转录失败: " + e.getMessage(), e);
        }
    }
    
    public static boolean isWhisperAvailable()
    {
        try
        {
            Request.Builder requestBuilder = new Request.Builder()
                .url(WHISPER_API_URL.replace("/v1/audio/transcriptions", "/health"))
                .get();
            
            if (WHISPER_API_KEY != null && !WHISPER_API_KEY.isEmpty())
            {
                requestBuilder.addHeader("Authorization", "Bearer " + WHISPER_API_KEY);
            }
            
            Request request = requestBuilder.build();
            
            try (Response response = client.newCall(request).execute())
            {
                return response.isSuccessful();
            }
        }
        catch (Exception e)
        {
            log.warn("【任务3-Whisper】Whisper API 不可用: {}", e.getMessage());
            return false;
        }
    }
    
    public static String getConfig()
    {
        return String.format("Whisper API URL: %s, API Key: %s", 
            WHISPER_API_URL, 
            WHISPER_API_KEY != null && !WHISPER_API_KEY.isEmpty() ? "已配置" : "未配置");
    }
}
