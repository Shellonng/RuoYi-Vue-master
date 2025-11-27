package com.ruoyi.system.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.config.AIConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.service.ITeachingPlanService;

/**
 * 教学计划服务实现
 * 
 * @author ruoyi
 */
@Service
public class TeachingPlanServiceImpl implements ITeachingPlanService
{
    private static final Logger logger = LoggerFactory.getLogger(TeachingPlanServiceImpl.class);
    
    @Autowired
    private AIConfig aiConfig;
    
    @Override
    public Map<String, Object> generateTeachingPlan(Map<String, Object> params) throws Exception
    {
        Long courseId = Long.parseLong(params.get("courseId").toString());
        String courseTitle = (String) params.get("courseTitle");
        List<Map<String, Object>> courseStructure = (List<Map<String, Object>>) params.get("courseStructure");
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");
        
        logger.info("开始为课程 {} 生成教学计划，课程时间：{} 至 {}", courseTitle, startTime, endTime);
        
        // 构建提示词
        String prompt = buildPrompt(courseTitle, courseStructure, startTime, endTime);
        
        // 调用通义千问API
        String aiResponse = callQwenAPI(prompt);
        
        // 解析AI返回的教学计划
        Map<String, Object> planData = parseAIResponse(aiResponse, courseStructure);
        
        return planData;
    }
    
    /**
     * 构建提示词
     */
    private String buildPrompt(String courseTitle, List<Map<String, Object>> courseStructure, String startTime, String endTime)
    {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位经验丰富的教学计划专家。请为以下课程安排一个合理的教学计划。\n\n");
        prompt.append("课程名称：").append(courseTitle).append("\n");
        prompt.append("课程时间：").append(startTime).append(" 至 ").append(endTime).append("\n\n");
        prompt.append("课程结构：\n");
        
        int chapterIndex = 1;
        for (Map<String, Object> chapter : courseStructure)
        {
            String chapterTitle = (String) chapter.get("chapterTitle");
            List<Map<String, Object>> sections = (List<Map<String, Object>>) chapter.get("sections");
            
            prompt.append(chapterIndex).append(". ").append(chapterTitle).append("\n");
            if (sections != null && !sections.isEmpty())
            {
                for (Map<String, Object> section : sections)
                {
                    prompt.append("   - ").append(section.get("sectionTitle")).append("\n");
                }
            }
            chapterIndex++;
        }
        
        prompt.append("\n请为这门课程的**每个章节**生成一个教学日程安排，要求：\n");
        prompt.append("1. **必须在课程时间范围内安排**：从 ").append(startTime).append(" 开始，到 ").append(endTime).append(" 结束\n");
        prompt.append("2. **必须为上面列出的每个章节都生成一条记录**，章节数量和上面列出的完全一致\n");
        prompt.append("3. **教学约束条件**：\n");
        prompt.append("   - 老师每周上两节课，每节课90分钟\n");
        prompt.append("   - 根据章节名称和包含的小节内容，合理判断该章节需要的教学时长\n");
        prompt.append("   - 有的小节内容简单可能一节课讲多个，有的复杂可能需要一节课甚至多节课\n");
        prompt.append("   - duration（持续天数）应该根据章节实际内容难度和深度来设定，而不是简单按小节数量\n");
        prompt.append("   - 章节间留出适当的复习和过渡时间（建议3-7天）\n");
        prompt.append("   - 所有章节的教学安排必须在课程结束时间之前完成\n");
        prompt.append("4. 返回的JSON数组长度必须等于章节总数（").append(courseStructure.size()).append("个章节）\n");
        prompt.append("5. JSON格式如下：\n");
        prompt.append("```json\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"chapterIndex\": 0,\n");
        prompt.append("    \"chapterTitle\": \"第一章标题\",\n");
        prompt.append("    \"date\": \"2025-11-27\",\n");
        prompt.append("    \"duration\": 10,\n");
        prompt.append("    \"description\": \"本章节学习重点\"\n");
        prompt.append("  },\n");
        prompt.append("  {\n");
        prompt.append("    \"chapterIndex\": 1,\n");
        prompt.append("    \"chapterTitle\": \"第二章标题\",\n");
        prompt.append("    \"date\": \"2025-12-07\",\n");
        prompt.append("    \"duration\": 14,\n");
        prompt.append("    \"description\": \"本章节学习重点\"\n");
        prompt.append("  }\n");
        prompt.append("]\n");
        prompt.append("```\n");
        prompt.append("6. chapterIndex从0开始，与上面章节列表顺序一一对应\n");
        prompt.append("7. 注意：第二章的date应该是第一章date + duration + 过渡时间（3-4天），以此类推\n");
        prompt.append("8. 只返回纯JSON数组，不要有任何其他文字或markdown标记");
        
        return prompt.toString();
    }
    
    /**
     * 调用通义千问API
     */
    private String callQwenAPI(String prompt) throws Exception
    {
        if (!aiConfig.getEnabled())
        {
            throw new ServiceException("AI功能未启用");
        }
        
        try
        {
            Generation gen = new Generation();
            
            // 构建系统提示词
            Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一位经验丰富的教学计划专家。你的任务是根据课程结构生成合理的教学日程安排。" +
                        "重要：你必须且只能返回纯JSON格式的数组，不要添加任何解释、说明或markdown标记，直接返回JSON数组。")
                .build();
            
            // 构建用户提示词
            Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(prompt)
                .build();
            
            // 设置生成参数
            GenerationParam param = GenerationParam.builder()
                .apiKey(aiConfig.getApiKey())
                .model(aiConfig.getModel())
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .temperature(0.7f)
                .build();
            
            // 调用AI
            logger.info("开始调用通义千问API生成教学计划，模型：{}", aiConfig.getModel());
            
            GenerationResult result = gen.call(param);
            
            // 检查响应
            if (result == null || result.getOutput() == null)
            {
                logger.error("AI返回结果为空");
                throw new ServiceException("AI服务返回结果为空");
            }
            
            if (result.getOutput().getChoices() == null || result.getOutput().getChoices().isEmpty())
            {
                logger.error("AI返回的choices为空");
                throw new ServiceException("AI服务返回的内容为空");
            }
            
            // 提取响应内容
            String responseContent = result.getOutput().getChoices().get(0).getMessage().getContent();
            logger.info("通义千问API返回内容长度：{}", responseContent != null ? responseContent.length() : 0);
            
            if (responseContent == null || responseContent.trim().isEmpty())
            {
                logger.error("AI响应内容为空");
                throw new ServiceException("AI返回的内容为空");
            }
            
            return responseContent;
        }
        catch (NoApiKeyException e)
        {
            logger.error("API Key未设置", e);
            throw new ServiceException("AI API Key配置错误");
        }
        catch (InputRequiredException e)
        {
            logger.error("输入参数缺失", e);
            throw new ServiceException("AI调用参数错误");
        }
        catch (ApiException e)
        {
            logger.error("通义千问API调用失败", e);
            throw new ServiceException("AI服务调用失败：" + e.getMessage());
        }
    }
    
    /**
     * 解析AI返回的教学计划
     */
    private Map<String, Object> parseAIResponse(String aiResponse, List<Map<String, Object>> courseStructure) throws Exception
    {
        // 提取JSON部分
        String jsonStr = extractJSON(aiResponse);
        
        JSONArray planArray = JSON.parseArray(jsonStr);
        
        // 验证返回的章节数量是否匹配
        if (planArray.size() != courseStructure.size())
        {
            logger.warn("AI返回的章节数量({})与实际章节数量({})不匹配，将进行调整", 
                       planArray.size(), courseStructure.size());
        }
        
        // 构建ECharts示例代码需要的数据格式
        // graphData格式: [['日期', 数值, '章节标题'], ...]
        List<Object[]> graphData = new ArrayList<>();
        List<Map<String, Integer>> links = new ArrayList<>();
        List<List<Object[]>> chapterDataList = new ArrayList<>(); // 每个章节的时间段数据
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = null;
        String endDate = null;
        
        // 确保为每个实际章节都生成数据
        int actualChapterCount = Math.min(planArray.size(), courseStructure.size());
        
        for (int i = 0; i < actualChapterCount; i++)
        {
            JSONObject item = planArray.getJSONObject(i);
            String date = item.getString("date");
            String chapterTitle = item.getString("chapterTitle");
            Integer duration = item.getInteger("duration");
            
            // 如果AI返回的章节标题与实际不符，使用实际的章节标题
            String actualChapterTitle = (String) courseStructure.get(i).get("chapterTitle");
            
            // 记录日期范围
            if (startDate == null || date.compareTo(startDate) < 0)
            {
                startDate = date;
            }
            
            // 计算该章节的结束日期
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(date));
            int actualDuration = duration != null ? duration : 3;
            calendar.add(Calendar.DATE, actualDuration);
            String chapterEndDate = sdf.format(calendar.getTime());
            
            if (endDate == null || chapterEndDate.compareTo(endDate) > 0)
            {
                endDate = chapterEndDate;
            }
            
            // graphData格式: [日期, 数值, 章节标题] - 数值用于在图上标记点的大小
            // 添加章节标题方便前端显示tooltip
            graphData.add(new Object[]{
                date, 
                actualDuration * 100,
                actualChapterTitle,
                i  // 添加章节索引
            });
            
            // 为该章节的每一天生成数据（用于热力图着色）
            // 包括从当前章节开始到下一章节开始之间的所有日期
            List<Object[]> chapterData = new ArrayList<>();
            Calendar dayCalendar = Calendar.getInstance();
            dayCalendar.setTime(sdf.parse(date));
            
            // 计算到下一章节开始的天数（如果是最后一章，使用duration）
            int daysToNextChapter = actualDuration;
            if (i < actualChapterCount - 1)
            {
                // 获取下一章节的开始日期
                JSONObject nextItem = planArray.getJSONObject(i + 1);
                String nextDate = nextItem.getString("date");
                Date nextStartDate = sdf.parse(nextDate);
                long diffInMillies = nextStartDate.getTime() - dayCalendar.getTime().getTime();
                daysToNextChapter = (int) (diffInMillies / (1000 * 60 * 60 * 24));
            }
            
            for (int day = 0; day < daysToNextChapter; day++)
            {
                String dayStr = sdf.format(dayCalendar.getTime());
                // 使用章节索引+1作为值，前端用于区分颜色
                chapterData.add(new Object[]{dayStr, (i + 1) * 100});
                dayCalendar.add(Calendar.DATE, 1);
            }
            chapterDataList.add(chapterData);
            
            // 更新结束日期
            dayCalendar.add(Calendar.DATE, -1);  // 回退一天到实际最后一天
            String actualEndDate = sdf.format(dayCalendar.getTime());
            if (endDate == null || actualEndDate.compareTo(endDate) > 0)
            {
                endDate = actualEndDate;
            }
            
            // 构建连接关系
            if (i < actualChapterCount - 1)
            {
                Map<String, Integer> link = new HashMap<>();
                link.put("source", i);
                link.put("target", i + 1);
                links.add(link);
            }
        }
        
        // 生成最后一个月的灰色背景数据（未被章节占用的日期）
        List<Object[]> backgroundData = generateLastMonthBackground(endDate, chapterDataList);
        
        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("graphData", graphData);
        result.put("links", links);
        result.put("dateRange", new String[]{startDate, endDate});
        result.put("chapterDataList", chapterDataList);
        result.put("backgroundData", backgroundData);
        
        logger.info("教学计划生成完成，共{}个章节", graphData.size());
        
        return result;
    }
    
    /**
     * 生成最后一个月的灰色背景数据（未被章节占用的日期）
     */
    private List<Object[]> generateLastMonthBackground(String endDateStr, List<List<Object[]>> chapterDataList) throws Exception
    {
        List<Object[]> backgroundData = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        Date endDate = sdf.parse(endDateStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        
        // 获取最后一个月的第一天和最后一天
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        calendar.set(year, month, 1);
        Date monthStart = calendar.getTime();
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date monthEnd = calendar.getTime();
        
        // 收集所有章节占用的日期
        Set<String> occupiedDates = new HashSet<>();
        for (List<Object[]> chapterData : chapterDataList)
        {
            for (Object[] dayData : chapterData)
            {
                occupiedDates.add((String) dayData[0]);
            }
        }
        
        // 遍历最后一个月的每一天
        calendar.setTime(monthStart);
        while (!calendar.getTime().after(monthEnd))
        {
            String dateStr = sdf.format(calendar.getTime());
            // 如果该日期未被章节占用，添加为背景数据（值为0，前端显示为灰色）
            if (!occupiedDates.contains(dateStr))
            {
                backgroundData.add(new Object[]{dateStr, 0});
            }
            calendar.add(Calendar.DATE, 1);
        }
        
        return backgroundData;
    }
    
    /**
     * 生成背景数据（填充所有日期，未被章节占用的显示为灰色）
     */
    private List<Object[]> generateBackgroundData(String startDateStr, String endDateStr, List<List<Object[]>> chapterDataList) throws Exception
    {
        List<Object[]> backgroundData = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        Date startDate = sdf.parse(startDateStr);
        Date endDate = sdf.parse(endDateStr);
        
        // 收集所有章节占用的日期
        Set<String> occupiedDates = new HashSet<>();
        for (List<Object[]> chapterData : chapterDataList)
        {
            for (Object[] dayData : chapterData)
            {
                occupiedDates.add((String) dayData[0]);
            }
        }
        
        // 从起始日期到结束日期的每一天
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        
        while (!calendar.getTime().after(endDate))
        {
            String dateStr = sdf.format(calendar.getTime());
            // 如果该日期未被章节占用，添加为背景数据（值为0，前端显示为灰色）
            if (!occupiedDates.contains(dateStr))
            {
                backgroundData.add(new Object[]{dateStr, 0});
            }
            calendar.add(Calendar.DATE, 1);
        }
        
        return backgroundData;
    }
    
    /**
     * 生成虚拟数据用于热力图背景
     */
    private List<Object[]> generateVirtualData(String startDateStr, String endDateStr) throws Exception
    {
        List<Object[]> data = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        Date startDate = sdf.parse(startDateStr);
        Date endDate = sdf.parse(endDateStr);
        
        // 从起始日期到结束日期的每一天
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        
        while (!calendar.getTime().after(endDate))
        {
            String dateStr = sdf.format(calendar.getTime());
            // 随机数值用于热力图背景色
            int value = (int)(Math.random() * 1000);
            data.add(new Object[]{dateStr, value});
            
            calendar.add(Calendar.DATE, 1);
        }
        
        return data;
    }
    
    /**
     * 从响应中提取JSON内容（复用AIService的逻辑）
     */
    private String extractJSON(String content)
    {
        if (content == null || content.trim().isEmpty())
        {
            throw new ServiceException("AI返回内容为空");
        }
        
        content = content.trim();
        
        // 移除可能的markdown代码块标记
        if (content.contains("```json"))
        {
            int start = content.indexOf("```json") + 7;
            int end = content.indexOf("```", start);
            if (end > start)
            {
                content = content.substring(start, end).trim();
            }
        }
        else if (content.contains("```"))
        {
            int start = content.indexOf("```") + 3;
            int end = content.indexOf("```", start);
            if (end > start)
            {
                content = content.substring(start, end).trim();
            }
        }
        
        // 查找JSON数组的开始和结束
        int start = content.indexOf("[");
        int end = content.lastIndexOf("]");
        
        if (start < 0 || end <= start)
        {
            logger.error("无法从AI响应中提取JSON数组。响应内容：{}", content);
            throw new ServiceException("AI返回的内容不是有效的JSON格式");
        }
        
        String jsonContent = content.substring(start, end + 1);
        
        // 验证是否是有效的JSON
        try
        {
            JSON.parse(jsonContent);
            return jsonContent;
        }
        catch (Exception e)
        {
            logger.error("提取的内容不是有效的JSON。内容：{}", jsonContent, e);
            throw new ServiceException("AI返回的JSON格式无效：" + e.getMessage());
        }
    }
}
