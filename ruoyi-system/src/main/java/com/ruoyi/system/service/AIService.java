package com.ruoyi.system.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.protocol.Protocol;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.ruoyi.common.config.AIConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.dto.CourseStructureDTO;

/**
 * AI服务 - 处理PDF提取和课程结构生成
 *
 * @author ruoyi
 */
@Service
public class AIService
{
    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    @Autowired
    private AIConfig aiConfig;

    /**
     * 根据文件类型提取文本
     *
     * @param file 文件
     * @return 提取的文本内容
     */
    public String extractTextFromFile(File file) throws IOException
    {
        String fileName = file.getName().toLowerCase();
        log.info("开始提取文件文本，文件名：{}，文件大小：{}字节", fileName, file.length());
        
        String text;
        
        try
        {
            if (fileName.endsWith(".pdf"))
            {
                text = extractTextFromPDF(file);
            }
            else if (fileName.endsWith(".docx"))
            {
                text = extractTextFromDocx(file);
            }
            else if (fileName.endsWith(".doc"))
            {
                text = extractTextFromDoc(file);
            }
            else if (fileName.endsWith(".pptx"))
            {
                text = extractTextFromPptx(file);
            }
            else if (fileName.endsWith(".ppt"))
            {
                text = extractTextFromPpt(file);
            }
            else
            {
                throw new IOException("不支持的文件格式: " + fileName);
            }
            
            if (text == null || text.trim().isEmpty())
            {
                log.error("提取的文本为空，文件：{}", fileName);
                throw new IOException("无法从文件中提取文本内容");
            }
            
            log.info("文本提取成功，原始长度：{}字符", text.length());
            
            // 限制文本长度，避免超出AI模型限制
            if (text.length() > 30000)
            {
                text = text.substring(0, 30000);
                log.warn("文档文本过长，已截取前30000字符");
            }
            
            return text;
        }
        catch (Exception e)
        {
            log.error("提取文件文本失败，文件：{}", fileName, e);
            throw new IOException("提取文件文本失败：" + e.getMessage(), e);
        }
    }

    /**
     * 从PDF文件提取文本
     */
    private String extractTextFromPDF(File pdfFile) throws IOException
    {
        try (PDDocument document = PDDocument.load(pdfFile))
        {
            PDFTextStripper stripper = new PDFTextStripper();
            // 限制提取前50页，避免内容过长
            stripper.setEndPage(Math.min(document.getNumberOfPages(), 50));
            return stripper.getText(document);
        }
    }

    /**
     * 从Word文档(.docx)提取文本
     */
    private String extractTextFromDocx(File docxFile) throws IOException
    {
        try (FileInputStream fis = new FileInputStream(docxFile);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document))
        {
            return extractor.getText();
        }
    }

    /**
     * 从Word文档(.doc)提取文本
     */
    private String extractTextFromDoc(File docFile) throws IOException
    {
        try (FileInputStream fis = new FileInputStream(docFile);
             HWPFDocument document = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(document))
        {
            return extractor.getText();
        }
    }

    /**
     * 从PowerPoint文档(.pptx)提取文本
     */
    private String extractTextFromPptx(File pptxFile) throws IOException
    {
        StringBuilder text = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(pptxFile);
             XMLSlideShow ppt = new XMLSlideShow(fis))
        {
            for (XSLFSlide slide : ppt.getSlides())
            {
                for (XSLFShape shape : slide.getShapes())
                {
                    if (shape instanceof XSLFTextShape)
                    {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        text.append(textShape.getText()).append("\n");
                    }
                }
            }
        }
        return text.toString();
    }

    /**
     * 从PowerPoint文档(.ppt)提取文本
     */
    private String extractTextFromPpt(File pptFile) throws IOException
    {
        StringBuilder text = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(pptFile);
             HSLFSlideShow ppt = new HSLFSlideShow(fis))
        {
            for (HSLFSlide slide : ppt.getSlides())
            {
                for (HSLFShape shape : slide.getShapes())
                {
                    if (shape instanceof HSLFTextShape)
                    {
                        HSLFTextShape textShape = (HSLFTextShape) shape;
                        text.append(textShape.getText()).append("\n");
                    }
                }
            }
        }
        return text.toString();
    }

    /**
     * 调用AI生成课程结构
     *
     * @param documentText 文档提取的文本
     * @param courseName 课程名称
     * @return 课程结构DTO
     */
    public CourseStructureDTO generateCourseStructure(String documentText, String courseName)
    {
        if (!aiConfig.getEnabled())
        {
            throw new ServiceException("AI功能未启用");
        }

        try
        {
            Generation gen = new Generation(Protocol.HTTP.getValue(), "https://dashscope.aliyuncs.com/api/v1");
            
            // 构建系统提示词
            Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一个专业的教育课程结构分析专家。你的任务是分析教材内容，提取课程的章节结构、小节内容和知识点信息。" +
                        "重要：你必须且只能返回纯JSON格式的数据，不要添加任何解释、说明或markdown标记，直接返回JSON对象。")
                .build();
            
            // 构建用户提示词
            String userPrompt = buildPrompt(documentText, courseName);
            Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(userPrompt)
                .build();
            
            // 设置生成参数，明确要求JSON格式
            GenerationParam param = GenerationParam.builder()
                .apiKey(aiConfig.getApiKey())
                .model(aiConfig.getModel())
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .temperature(0.7f)
                .build();
            
            // 调用AI
            log.info("开始调用AI生成课程结构，课程名称：{}，模型：{}，文本长度：{}", 
                courseName, aiConfig.getModel(), documentText.length());
            
            GenerationResult result = gen.call(param);
            
            // 检查响应
            if (result == null || result.getOutput() == null)
            {
                log.error("AI返回结果为空");
                throw new ServiceException("AI服务返回结果为空");
            }
            
            if (result.getOutput().getChoices() == null || result.getOutput().getChoices().isEmpty())
            {
                log.error("AI返回的choices为空");
                throw new ServiceException("AI服务返回的内容为空");
            }
            
            // 提取响应内容
            String responseContent = result.getOutput().getChoices().get(0).getMessage().getContent();
            log.info("AI响应成功，内容长度：{}，前200字符：{}", 
                responseContent != null ? responseContent.length() : 0,
                responseContent != null && responseContent.length() > 200 ? responseContent.substring(0, 200) : responseContent);
            
            if (responseContent == null || responseContent.trim().isEmpty())
            {
                log.error("AI响应内容为空");
                throw new ServiceException("AI返回的内容为空");
            }
            
            // 解析JSON响应
            return parseAIResponse(responseContent);
        }
        catch (ApiException | NoApiKeyException | InputRequiredException e)
        {
            log.error("AI调用失败", e);
            throw new ServiceException("AI生成课程结构失败：" + e.getMessage());
        }
    }

    /**
     * 构建AI提示词
     */
    private String buildPrompt(String documentText, String courseName)
    {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下《").append(courseName).append("》教材的内容,提取出课程的完整结构。\n\n");
        prompt.append("【重要】直接返回纯JSON格式,不要有任何markdown标记(如```json)、不要有任何解释文字,只返回JSON对象。\n\n");
        prompt.append("要求:\n");
        prompt.append("1. 识别所有章节(Chapter),提取章节名称和描述\n");
        prompt.append("2. 【重要】详细拆分每个章节下的小节(Section),尽可能细化小节划分,每个独立的主题或概念都应该是一个小节\n");
        prompt.append("3. 为每个小节提取核心知识点(Knowledge Point),每个小节包含3-5个知识点\n");
        prompt.append("4. 知识点应该是具体的概念、定义、原理或方法,不要太笼统\n");
        prompt.append("5. 难度级别分为三类:BASIC(基础)、INTERMEDIATE(中级)、ADVANCED(高级)\n");
        prompt.append("6. 为章节、小节和知识点分配合理的排序顺序\n");
        prompt.append("7. 描述要简洁明了,每个描述控制在50字以内\n\n");
        prompt.append("返回格式示例（直接返回此格式的JSON，不要加```符号）：\n");
        prompt.append("{\n");
        prompt.append("  \"chapters\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"第一章 章节名称\",\n");
        prompt.append("      \"description\": \"章节描述\",\n");
        prompt.append("      \"sortOrder\": 1,\n");
        prompt.append("      \"sections\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"title\": \"1.1 小节名称\",\n");
        prompt.append("          \"description\": \"小节描述\",\n");
        prompt.append("          \"sortOrder\": 1,\n");
        prompt.append("          \"knowledgePoints\": [\n");
        prompt.append("            {\n");
        prompt.append("              \"title\": \"知识点1\",\n");
        prompt.append("              \"description\": \"知识点描述\",\n");
        prompt.append("              \"level\": \"BASIC\",\n");
        prompt.append("              \"sequence\": 1\n");
        prompt.append("            },\n");
        prompt.append("            {\n");
        prompt.append("              \"title\": \"知识点2\",\n");
        prompt.append("              \"description\": \"知识点描述\",\n");
        prompt.append("              \"level\": \"INTERMEDIATE\",\n");
        prompt.append("              \"sequence\": 2\n");
        prompt.append("            },\n");
        prompt.append("            {\n");
        prompt.append("              \"title\": \"知识点3\",\n");
        prompt.append("              \"description\": \"知识点描述\",\n");
        prompt.append("              \"level\": \"ADVANCED\",\n");
        prompt.append("              \"sequence\": 3\n");
        prompt.append("            }\n");
        prompt.append("          ]\n");
        prompt.append("        }\n");
        prompt.append("      ]\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");
        prompt.append("教材内容如下：\n");
        prompt.append("---\n");
        prompt.append(documentText);
        
        return prompt.toString();
    }

    /**
     * 解析AI响应
     */
    private CourseStructureDTO parseAIResponse(String responseContent)
    {
        try
        {
            // 尝试提取JSON部分（AI可能返回markdown格式）
            String jsonContent = extractJSON(responseContent);
            
            // 解析JSON
            CourseStructureDTO structure = JSON.parseObject(jsonContent, CourseStructureDTO.class);
            
            if (structure == null || structure.getChapters() == null || structure.getChapters().isEmpty())
            {
                throw new ServiceException("AI返回的课程结构为空");
            }
            
            return structure;
        }
        catch (JSONException e)
        {
            log.error("解析AI响应失败，响应内容：{}", responseContent, e);
            throw new ServiceException("解析AI响应失败：" + e.getMessage());
        }
    }

    /**
     * 从响应中提取JSON内容
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
        
        // 查找JSON对象的开始和结束
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");
        
        if (start < 0 || end <= start)
        {
            log.error("无法从AI响应中提取JSON对象。响应内容：{}", content);
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
            log.error("提取的内容不是有效的JSON。内容：{}", jsonContent, e);
            throw new ServiceException("AI返回的JSON格式无效：" + e.getMessage());
        }
    }
}
