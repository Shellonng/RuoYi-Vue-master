package com.ruoyi.system.service.impl;

import java.io.IOException;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.service.ITtsService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

/**
 * 文字转语音服务实现
 * 使用阿里云DashScope HTTP API
 * 
 * @author ruoyi
 */
@Service
public class TtsServiceImpl implements ITtsService 
{
    private static final Logger log = LoggerFactory.getLogger(TtsServiceImpl.class);
    
    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";
    private static final String MODEL = "qwen3-tts-flash";
    private static final String API_KEY = "sk-abf39dd471804664b5dce35e722f0857";
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    
    @Override
    public String textToSpeech(String text) 
    {
        return textToSpeech(text, "CHERRY");
    }
    
    @Override
    public String textToSpeech(String text, String voice) 
    {
        try 
        {
            log.info("开始语音合成，文本：{}", text);
            
            // 判断语言类型
            String languageType = containsChinese(text) ? "Chinese" : "English";
            
            // 构建请求JSON
            JSONObject requestJson = new JSONObject();
            requestJson.put("model", MODEL);
            
            JSONObject input = new JSONObject();
            input.put("text", text);
            requestJson.put("input", input);
            
            JSONObject parameters = new JSONObject();
            parameters.put("voice", voice);
            parameters.put("language_type", languageType);
            requestJson.put("parameters", parameters);
            
            log.info("调用DashScope TTS API，模型：{}，语音：{}，语言：{}", MODEL, voice, languageType);
            
            // 发送HTTP POST请求
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    requestJson.toJSONString()
            );
            
            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) 
            {
                if (!response.isSuccessful()) 
                {
                    String errorBody = response.body() != null ? response.body().string() : "无响应体";
                    log.error("API调用失败，HTTP状态码：{}，响应：{}", response.code(), errorBody);
                    throw new ServiceException("语音合成失败：HTTP " + response.code());
                }
                
                String responseBody = response.body().string();
                log.info("API调用成功，响应长度：{}", responseBody.length());
                
                JSONObject result = JSON.parseObject(responseBody);
                JSONObject output = result.getJSONObject("output");
                
                if (output != null) 
                {
                    JSONObject audio = output.getJSONObject("audio");
                    if (audio != null) 
                    {
                        String audioUrl = audio.getString("url");
                        
                        if (audioUrl != null && !audioUrl.isEmpty()) 
                        {
                            log.info("获取到音频URL：{}", audioUrl);
                            // 下载音频并编码为Base64返回（避免跨域问题）
                            return downloadAndEncodeAudio(audioUrl);
                        }
                    }
                }
                
                log.error("语音合成失败：未获取到音频URL，响应：{}", responseBody);
                throw new ServiceException("语音合成失败：未获取到音频URL");
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (IOException e) 
        {
            log.error("文字转语音网络请求失败", e);
            throw new ServiceException("语音合成失败：网络请求失败");
        }
        catch (Exception e) 
        {
            log.error("文字转语音失败", e);
            throw new ServiceException("语音合成失败：" + e.getMessage());
        }
    }
    
    /**
     * 下载音频文件并编码为Base64
     */
    private String downloadAndEncodeAudio(String audioUrl) throws IOException 
    {
        Request request = new Request.Builder()
                .url(audioUrl)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) 
        {
            if (response.isSuccessful() && response.body() != null) 
            {
                byte[] audioData = response.body().bytes();
                log.info("音频下载成功，大小：{} 字节", audioData.length);
                return Base64.getEncoder().encodeToString(audioData);
            }
            else
            {
                throw new ServiceException("下载音频失败：HTTP " + response.code());
            }
        }
    }
    
    /**
     * 判断文本是否包含中文
     */
    private boolean containsChinese(String text) 
    {
        if (text == null || text.isEmpty()) 
        {
            return false;
        }
        for (char c : text.toCharArray()) 
        {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) 
            {
                return true;
            }
        }
        return false;
    }
}
