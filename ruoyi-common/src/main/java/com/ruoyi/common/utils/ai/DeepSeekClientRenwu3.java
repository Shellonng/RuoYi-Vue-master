package com.ruoyi.common.utils.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeepSeekClientRenwu3
{
    private static final Logger log = LoggerFactory.getLogger(DeepSeekClientRenwu3.class);

    // 智谱AI API配置
    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String API_KEY = "c49af7163d98498f80257dc8743c12e0.TFIgi0KDRYG68nMh";
    
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public static List<String> extractKnowledgePoints(String documentText, String courseTitle)
    {
        // 构建Prompt
        String prompt = buildKnowledgeExtractionPrompt(documentText, courseTitle);
        
        try
        {
            // 调用智谱AI API
            String response = callDeepSeekAPI(prompt);
            
            // 解析返回结果
            return parseKnowledgePointsFromResponse(response);
        }
        catch (Exception e)
        {
            log.error("调用智谱AI API失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private static String buildKnowledgeExtractionPrompt(String documentText, String courseTitle)
    {
        // 限制文本长度，避免token超限（DeepSeek最大约32K tokens）
        int maxLength = 8000; // 约6000个tokens
        if (documentText.length() > maxLength)
        {
            documentText = documentText.substring(0, maxLength) + "...";
        }

        return String.format(
            "你是一位专业的教育领域AI助手。请从以下课程资料中提取核心知识点。\n\n" +
            "**课程名称**: %s\n\n" +
            "**资料内容**:\n%s\n\n" +
            "**任务要求**:\n" +
            "1. 提取文档中的核心知识点（5-15个）\n" +
            "2. 知识点名称要简洁明确（3-8个字）\n" +
            "3. 只返回知识点列表，每个知识点占一行\n" +
            "4. 不要添加序号、标点或其他修饰\n" +
           "你是一位专业且友好的教育AI助手。你刚刚帮助老师完成了课程资源的智能分析。\n\n" +
                "**分析情况**:\n%s\n\n" +
                "**老师的问题**: %s\n\n" +
                "请用通俗易懂、友好的语言回答老师的问题：\n" +
                "- 如果老师询问为什么提取了某个知识点，请分析该知识点在资料中的重要性和相关内容\n" +
                "- 如果老师对提取结果有疑问，请耐心解释AI的分析思路\n" +
                "- 如果老师询问如何改进，请给出具体的建议\n" +
                "- 保持专业但不要过于技术化",
            courseTitle, documentText
        );
    }

    private static String callDeepSeekAPI(String prompt) throws IOException
    {
        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "glm-4");  // 智谱AI GLM-4模型
        
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3); 
        requestBody.put("max_tokens", 1000);

        // 构建HTTP请求 (OkHttp 3.x 参数顺序: MediaType, String)
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, requestBody.toJSONString());

        Request request = new Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build();

        // 发送请求
        log.info("正在调用智谱AI API...");
        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful())
        {
            String errorBody = response.body() != null ? response.body().string() : "无响应体";
            log.error("智谱AI API调用失败, HTTP状态码: {}, 错误信息: {}", response.code(), errorBody);
            throw new IOException("智谱AI API调用失败: HTTP " + response.code());
        }

        String responseBody = response.body().string();
        log.info("智谱AI API调用成功");
        
        return responseBody;
    }

    private static List<String> parseKnowledgePointsFromResponse(String responseJson)
    {
        List<String> knowledgePoints = new ArrayList<>();
        
        try
        {
            JSONObject jsonObject = JSON.parseObject(responseJson);
            JSONArray choices = jsonObject.getJSONArray("choices");
            
            if (choices != null && choices.size() > 0)
            {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                String content = message.getString("content");
                
                // 按行分割，过滤空行
                String[] lines = content.split("\n");
                for (String line : lines)
                {
                    line = line.trim();
                    // 移除可能的序号（如"1. "、"- "等）
                    line = line.replaceAll("^[0-9]+[.、\\s]+", "");
                    line = line.replaceAll("^[-*]+\\s+", "");
                    
                    if (!line.isEmpty() && line.length() <= 50)
                    {
                        knowledgePoints.add(line);
                    }
                }
                
                log.info("从AI响应中解析出{}个知识点", knowledgePoints.size());
            }
        }
        catch (Exception e)
        {
            log.error("解析DeepSeek响应失败: {}", e.getMessage());
        }
        
        return knowledgePoints;
    }

    public static double calculateSimilarity(String str1, String str2)
    {
        if (str1 == null || str2 == null)
        {
            return 0.0;
        }
        
        str1 = str1.toLowerCase().trim();
        str2 = str2.toLowerCase().trim();
        
        if (str1.equals(str2))
        {
            return 1.0;
        }
        
        // 包含关系判断
        if (str1.contains(str2) || str2.contains(str1))
        {
            return 0.8;
        }
        
        // 计算Levenshtein距离
        int distance = levenshteinDistance(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());
        
        return 1.0 - ((double) distance / maxLength);
    }

    private static int levenshteinDistance(String s1, String s2)
    {
        int len1 = s1.length();
        int len2 = s2.length();
        
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        for (int i = 0; i <= len1; i++)
        {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= len2; j++)
        {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= len1; i++)
        {
            for (int j = 1; j <= len2; j++)
            {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[len1][len2];
    }

    /**
     * 与AI进行对话
     * 
     * @param userMessage 用户消息
     * @param contextInfo 上下文信息（课程名称、推荐结果等）
     * @return AI回复
     */
    public static String chatWithAI(String userMessage, String contextInfo)
    {
        try
        {
           
            String prompt = String.format(
                "你是一位专业且友好的教育AI助手。你刚刚帮助老师完成了课程资源的智能分析。\n\n" +
                "**分析情况**:\n%s\n\n" +
                "**老师的问题**: %s\n\n" +
                "请用通俗易懂、友好的语言回答老师的问题：\n" +
                "- 如果老师询问为什么提取了某个知识点，请分析该知识点在资料中的重要性和相关内容\n" +
                "- 如果老师对提取结果有疑问，请耐心解释AI的分析思路\n" +
                "- 如果老师询问如何改进，请给出具体的建议\n" +
                "- 保持专业但不要过于技术化，避免提及算法细节、阈值等专业术语",
                contextInfo, userMessage
            );
            
          
            String response = callDeepSeekAPI(prompt);
            
            
            JSONObject jsonObject = JSON.parseObject(response);
            JSONArray choices = jsonObject.getJSONArray("choices");
            
            if (choices != null && choices.size() > 0)
            {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                String content = message.getString("content");
                
                log.info("AI对话响应成功");
                return content;
            }
            
            return "抱歉，我暂时无法回答这个问题。";
        }
        catch (Exception e)
        {
            log.error("AI对话失败: {}", e.getMessage());
            return "抱歉，我遇到了一些问题，请稍后再试。";
        }
    }
}
