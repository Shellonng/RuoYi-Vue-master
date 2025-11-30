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
            Generation gen = new Generation();
            
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

    /**
     * AI生成知识点关系
     * 
     * @param knowledgePoints 知识点列表（JSON字符串，包含id和title）
     * @return 知识点关系列表的JSON字符串
     */
    public String generateKnowledgePointRelations(String knowledgePoints)
    {
        log.info("开始调用AI生成知识点关系");
        
        try
        {
            Generation gen = new Generation();
            
            log.info("Generation实例创建成功");

            // 构建提示词
            String systemPrompt = "你是一个专业的教育领域专家，擅长分析知识点之间的关系。请根据给定的知识点列表，深入分析它们之间的多种关系，**确保所有知识点形成一个连通图（每个知识点至少与一个其他知识点有关系）**。\n\n" +
                "关系类型说明（请尽可能使用所有类型）：\n" +
                "1. prerequisite_of: A是B的前置知识（学习B之前需要先学习A）—— **最重要，优先识别**\n" +
                "2. similar_to: A和B是相似的概念（主题相近、方法类似）\n" +
                "3. extension_of: A是B的扩展（A在B的基础上深化或拓展）\n" +
                "4. derived_from: A派生自B（A是从B推导、演化或应用得来的）\n" +
                "5. counterexample_of: A是B的反例或对立概念\n\n" +
                "请返回JSON格式，包含relations数组，每个关系包含：\n" +
                "- fromKpId: 源知识点ID\n" +
                "- toKpId: 目标知识点ID\n" +
                "- relationType: 关系类型（使用上述5种之一）\n" +
                "- reason: 简短说明（可选，不超过30字）\n\n" +
                "**分析要求（重要）：**\n" +
                "1. **全连通性（必须）**：确保生成的关系图是连通的，不能有孤立的知识点或分离的子图。每个知识点至少要与其他1-2个知识点建立关系\n" +
                "2. **丰富性**：尽量为每个知识点建立2-5个不同类型的关系\n" +
                "3. **多样性**：优先使用prerequisite_of和extension_of，但也要积极寻找similar_to、derived_from和counterexample_of关系\n" +
                "4. **双向关系**：如果A相似于B，也可以建立B相似于A（相似、推导关系）\n" +
                "5. **深度分析**：\n" +
                "   - prerequisite_of: 找出学习顺序的依赖关系\n" +
                "   - similar_to: 找出概念、方法、应用场景的相似性\n" +
                "   - extension_of: 找出难度递进、内容深化的关系\n" +
                "   - derived_from: 找出理论推导、公式变换、应用转化的关系\n" +
                "   - counterexample_of: 找出概念对立、方法对比的关系\n" +
                "6. **质量优先**：只返回确定性强、有意义的关系\n" +
                "7. **关系数量**：目标是生成知识点数量的2-4倍的关系（如10个知识点，生成20-40条关系），确保图的连通性和密度\n\n" +
                "返回纯JSON，不要包含其他说明文字。";

            String userPrompt = "知识点列表：\n" + knowledgePoints + "\n\n" +
                "请深入分析这些知识点之间的各种关系，**特别注意确保所有知识点连通，不能有孤立节点**，重点关注：\n" +
                "1. 学习顺序的先后依赖（prerequisite_of）\n" +
                "2. 概念和方法的相似性（similar_to）\n" +
                "3. 知识的递进和深化（extension_of）\n" +
                "4. 理论的推导和应用（derived_from）\n" +
                "5. 概念的对立和对比（counterexample_of）\n\n" +
                "请为每个知识点建立2-5个不同类型的关系，确保整个知识图谱是一个连通图，返回JSON格式的结果。";

            Message systemMessage = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(systemPrompt)
                    .build();

            Message userMessage = Message.builder()
                    .role(Role.USER.getValue())
                    .content(userPrompt)
                    .build();

            GenerationParam param = GenerationParam.builder()
                    .apiKey(aiConfig.getApiKey())
                    .model("qwen-plus")
                    .messages(Arrays.asList(systemMessage, userMessage))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .topP(0.8)  // 提高多样性
                    .temperature(0.5f)  // 平衡准确性和创造性
                    .build();

            log.info("开始调用通义千问API生成知识点关系");
            GenerationResult result = gen.call(param);
            log.info("API调用完成");

            String responseContent = result.getOutput().getChoices().get(0).getMessage().getContent();
            log.info("AI返回的知识点关系内容长度：{}，内容预览：{}", 
                responseContent.length(),
                responseContent.substring(0, Math.min(200, responseContent.length())));

            // 提取并验证JSON
            String jsonContent = extractJSON(responseContent);
            
            return jsonContent;
        }
        catch (NoApiKeyException e)
        {
            log.error("API Key未设置", e);
            throw new ServiceException("AI API Key配置错误");
        }
        catch (InputRequiredException e)
        {
            log.error("输入参数缺失", e);
            throw new ServiceException("AI调用参数错误");
        }
        catch (ApiException e)
        {
            log.error("AI API调用失败", e);
            throw new ServiceException("AI服务调用失败：" + e.getMessage());
        }
        catch (Exception e)
        {
            log.error("生成知识点关系时发生未知错误", e);
            throw new ServiceException("生成知识点关系失败：" + e.getMessage());
        }
    }

    /**
     * AI生成课程描述
     *
     * @param courseTitle 课程标题
     * @return 生成的课程描述
     */
    public String generateCourseDescription(String courseTitle)
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
                .content("你是一个专业的教育课程介绍撰写专家。你的任务是根据课程标题，生成一段简洁、专业、有吸引力的课程描述。" +
                        "课程描述应该包含：1)课程的核心内容和目标；2)适用对象；3)学习收获。" +
                        "要求：语言简洁明了，控制在100-150字之内，不要使用markdown格式，直接返回纯文本描述。")
                .build();
            
            // 构建用户提示词
            String userPrompt = String.format("请为以下课程生成简要描述：\n\n课程标题：%s\n\n" +
                    "请生成一段专业、简洁的课程描述（100-150字）。", courseTitle);
            
            Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(userPrompt)
                .build();
            
            // 设置生成参数
            GenerationParam param = GenerationParam.builder()
                .apiKey(aiConfig.getApiKey())
                .model(aiConfig.getModel())
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .temperature(0.7f)
                .maxTokens(500)
                .build();
            
            // 调用AI
            log.info("开始调用AI生成课程描述，课程标题：{}，模型：{}", courseTitle, aiConfig.getModel());
            
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
            String description = result.getOutput().getChoices().get(0).getMessage().getContent();
            
            if (description == null || description.trim().isEmpty())
            {
                log.error("AI响应内容为空");
                throw new ServiceException("AI返回的内容为空");
            }
            
            // 清理可能存在的多余空格和换行
            description = description.trim().replaceAll("\\s+", " ");
            
            log.info("课程描述生成成功，长度：{}，内容：{}", description.length(), description);
            
            return description;
        }
        catch (NoApiKeyException e)
        {
            log.error("API Key未设置", e);
            throw new ServiceException("AI API Key配置错误");
        }
        catch (InputRequiredException e)
        {
            log.error("输入参数缺失", e);
            throw new ServiceException("AI调用参数错误");
        }
        catch (ApiException e)
        {
            log.error("AI API调用失败", e);
            throw new ServiceException("AI服务调用失败：" + e.getMessage());
        }
        catch (Exception e)
        {
            log.error("生成课程描述时发生未知错误", e);
            throw new ServiceException("生成课程描述失败：" + e.getMessage());
        }
    }

    /**
     * AI生成知识点详解
     *
     * @param kpTitle 知识点名称
     * @return 生成的知识点详解（Markdown + KaTeX格式）
     */
    public String generateKnowledgePointDescription(String kpTitle)
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
                .content("你是一个专业的教育专家。你的任务是为指定的知识点生成详细、易懂的学习解析。" +
                        "\n\n格式要求：" +
                        "1. 不要使用一级标题(#)，从二级标题(##)开始" +
                        "2. 标题之间不要有空行或分隔线(---)" +
                        "3. 使用紧凑的段落格式，避免过多换行" +
                        "4. 如果涉及数学公式，使用 KaTeX 语法（行内公式用 $...$ ，块级公式用 $$...$$）" +
                        "5. 如果涉及代码，使用代码块 ```language\\ncode\\n``` 格式" +
                        "\n\n内容结构：" +
                        "## 定义与概念\n简要说明知识点的定义和核心概念" +
                        "\n\n## 关键要点\n列出重点和难点" +
                        "\n\n## 示例说明\n提供实际应用示例或代码演示" +
                        "\n\n## 注意事项\n说明常见误区和注意点" +
                        "\n\n语言要求：简洁明了，适合学生自学理解，控制在400-600字")
                .build();
            
            // 构建用户提示词
            String userPrompt = String.format(
                "请为以下知识点生成详细的学习解析：\\n\\n知识点名称：%s\\n\\n" +
                "请使用 Markdown 格式，必要时包含 KaTeX 公式（用 $ 或 $$ 包裹）和代码块（用 ``` 包裹）。", 
                kpTitle
            );
            
            Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(userPrompt)
                .build();
            
            // 设置生成参数
            GenerationParam param = GenerationParam.builder()
                .apiKey(aiConfig.getApiKey())
                .model(aiConfig.getModel())
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .temperature(0.7f)
                .maxTokens(1500)  // 增加token数以支持更详细的内容
                .build();
            
            // 调用AI
            log.info("开始调用AI生成知识点详解，知识点：{}，模型：{}", kpTitle, aiConfig.getModel());
            
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
            String description = result.getOutput().getChoices().get(0).getMessage().getContent();
            
            if (description == null || description.trim().isEmpty())
            {
                log.error("AI响应内容为空");
                throw new ServiceException("AI返回的内容为空");
            }
            
            // 保留Markdown格式，只清理首尾空白
            description = description.trim();
            
            log.info("知识点详解生成成功，长度：{}", description.length());
            log.debug("生成的内容：{}", description);
            
            return description;
        }
        catch (NoApiKeyException e)
        {
            log.error("API Key未设置", e);
            throw new ServiceException("AI API Key配置错误");
        }
        catch (InputRequiredException e)
        {
            log.error("输入参数缺失", e);
            throw new ServiceException("AI调用参数错误");
        }
        catch (ApiException e)
        {
            log.error("AI API调用失败", e);
            throw new ServiceException("AI服务调用失败：" + e.getMessage());
        }
        catch (Exception e)
        {
            log.error("生成知识点详解时发生未知错误", e);
            throw new ServiceException("生成知识点详解失败：" + e.getMessage());
        }
    }

    /**
     * AI智能匹配知识点
     *
     * @param assignmentTitle 作业标题
     * @param assignmentDescription 作业描述
     * @param attachments 附件信息
     * @param availableKnowledgePoints 可选知识点列表
     * @return 匹配的知识点ID列表
     */
    @SuppressWarnings("unchecked")
    public java.util.List<Long> matchKnowledgePoints(
        String assignmentTitle, 
        String assignmentDescription,
        Object attachments,
        Object availableKnowledgePoints)
    {
        if (!aiConfig.getEnabled())
        {
            throw new ServiceException("AI功能未启用");
        }

        try
        {
            Generation gen = new Generation();
            
            // 构建知识点信息字符串
            StringBuilder kpInfoBuilder = new StringBuilder();
            java.util.List<java.util.Map<String, Object>> kpList = 
                (java.util.List<java.util.Map<String, Object>>) availableKnowledgePoints;
            
            if (kpList == null || kpList.isEmpty())
            {
                log.warn("可选知识点列表为空");
                return new java.util.ArrayList<>();
            }
            
            for (java.util.Map<String, Object> kp : kpList)
            {
                Long id = kp.get("id") instanceof Integer ? 
                    ((Integer) kp.get("id")).longValue() : (Long) kp.get("id");
                String title = (String) kp.get("title");
                String description = (String) kp.get("description");
                String level = (String) kp.get("level");
                
                kpInfoBuilder.append(String.format("ID:%d, 标题:%s, 描述:%s, 难度:%s\n", 
                    id, title, description != null ? description : "无", level != null ? level : "无"));
            }
            
            // 构建系统提示词
            Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一个专业的教育内容分析专家。你的任务是根据作业的标题、描述和附件信息，" +
                        "从给定的知识点列表中，智能匹配最相关的知识点。" +
                        "你需要深入理解作业内容涉及的知识领域，并准确找出对应的知识点。" +
                        "请只返回匹配的知识点ID，用逗号分隔，例如：1,3,5。不要返回其他任何内容。")
                .build();
            
            // 构建用户提示词
            StringBuilder userPrompt = new StringBuilder();
            userPrompt.append("请根据以下作业信息，从知识点列表中匹配最相关的知识点：\n\n");
            userPrompt.append("作业标题：").append(assignmentTitle != null ? assignmentTitle : "无").append("\n");
            userPrompt.append("作业描述：").append(assignmentDescription != null ? assignmentDescription : "无").append("\n");
            
            if (attachments != null)
            {
                userPrompt.append("附件信息：").append(JSON.toJSONString(attachments)).append("\n");
            }
            
            userPrompt.append("\n可选知识点列表：\n").append(kpInfoBuilder.toString());
            userPrompt.append("\n请返回匹配的知识点ID（用逗号分隔，例如：1,3,5）：");
            
            Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(userPrompt.toString())
                .build();
            
            // 设置生成参数
            GenerationParam param = GenerationParam.builder()
                .apiKey(aiConfig.getApiKey())
                .model(aiConfig.getModel())
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .temperature(0.3f)  // 较低的temperature以获得更确定的结果
                .maxTokens(200)
                .build();
            
            // 调用AI
            log.info("开始调用AI匹配知识点，作业：{}，可选知识点数：{}", 
                assignmentTitle, kpList.size());
            
            GenerationResult result = gen.call(param);
            
            // 检查响应
            if (result == null || result.getOutput() == null 
                || result.getOutput().getChoices() == null 
                || result.getOutput().getChoices().isEmpty())
            {
                log.error("AI返回结果为空");
                return new java.util.ArrayList<>();
            }
            
            // 提取响应内容
            String response = result.getOutput().getChoices().get(0).getMessage().getContent();
            
            if (response == null || response.trim().isEmpty())
            {
                log.warn("AI返回的内容为空");
                return new java.util.ArrayList<>();
            }
            
            // 解析ID列表
            java.util.List<Long> matchedIds = new java.util.ArrayList<>();
            String[] idStrings = response.trim().split("[,，\\s]+");
            
            for (String idStr : idStrings)
            {
                try
                {
                    // 清理可能的非数字字符
                    idStr = idStr.replaceAll("[^0-9]", "");
                    if (!idStr.isEmpty())
                    {
                        Long id = Long.parseLong(idStr);
                        // 验证ID是否在可选列表中
                        boolean exists = kpList.stream()
                            .anyMatch(kp -> {
                                Long kpId = kp.get("id") instanceof Integer ? 
                                    ((Integer) kp.get("id")).longValue() : (Long) kp.get("id");
                                return kpId.equals(id);
                            });
                        
                        if (exists && !matchedIds.contains(id))
                        {
                            matchedIds.add(id);
                        }
                    }
                }
                catch (NumberFormatException e)
                {
                    log.warn("无法解析ID：{}", idStr);
                }
            }
            
            log.info("AI成功匹配{}个知识点：{}", matchedIds.size(), matchedIds);
            
            return matchedIds;
        }
        catch (NoApiKeyException e)
        {
            log.error("API Key未设置", e);
            throw new ServiceException("AI API Key配置错误");
        }
        catch (ApiException e)
        {
            log.error("AI API调用失败", e);
            throw new ServiceException("AI服务调用失败：" + e.getMessage());
        }
        catch (Exception e)
        {
            log.error("匹配知识点时发生错误", e);
            throw new ServiceException("匹配知识点失败：" + e.getMessage());
        }
    }

    /**
     * 教学助手"小智"对话功能
     * 
     * @param userMessage 用户消息
     * @param messageHistory 消息历史记录
     * @return AI回复内容
     */
    public String chatWithTeachingAssistant(String userMessage, java.util.List<java.util.Map<String, String>> messageHistory)
    {
        try
        {
            log.info("教学助手小智处理消息，用户消息：{}", userMessage);

            // 构建系统提示词，定义小智的角色和能力
            String systemPrompt = "你是教学助手\"小智\"，专门为教师提供教学帮助。你的职责包括：\n" +
                "1. 解答教学相关问题，如课程设计、教学方法、学生管理等\n" +
                "2. 提供作业设计建议和评分标准\n" +
                "3. 帮助教师分析学生学习数据和表现\n" +
                "4. 推荐教学资源和工具\n" +
                "5. 协助处理日常教学管理任务\n" +
                "请以友好、专业的语气回答问题，提供实用的建议和解决方案。";

            // 构建消息列表
            java.util.List<Message> messages = new java.util.ArrayList<>();
            
            // 添加系统提示
            messages.add(Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(systemPrompt)
                .build());

            // 添加历史消息（保留最近10轮对话）
            int startIndex = Math.max(0, messageHistory.size() - 20); // 10轮对话 = 20条消息
            for (int i = startIndex; i < messageHistory.size(); i++)
            {
                java.util.Map<String, String> msg = messageHistory.get(i);
                String role = msg.get("role");
                String content = msg.get("content");
                
                if ("user".equals(role))
                {
                    messages.add(Message.builder()
                        .role(Role.USER.getValue())
                        .content(content)
                        .build());
                }
                else if ("assistant".equals(role))
                {
                    messages.add(Message.builder()
                        .role(Role.ASSISTANT.getValue())
                        .content(content)
                        .build());
                }
            }

            // 如果最后一条不是当前用户消息，则添加
            if (messageHistory.isEmpty() || !userMessage.equals(messageHistory.get(messageHistory.size() - 1).get("content")))
            {
                messages.add(Message.builder()
                    .role(Role.USER.getValue())
                    .content(userMessage)
                    .build());
            }

            // 构建请求参数
            GenerationParam param = GenerationParam.builder()
                .apiKey(aiConfig.getApiKey()) // 使用配置文件中的API Key
                .model(aiConfig.getModel()) // 使用配置文件中的模型
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .enableSearch(false) // 不启用搜索
                .build();

            log.info("调用通义千问API，消息数量：{}，模型：{}", messages.size(), aiConfig.getModel());

            // 调用API
            Generation gen = new Generation();
            GenerationResult result = gen.call(param);

            // 提取回复内容
            String reply = result.getOutput().getChoices().get(0).getMessage().getContent();
            
            if (reply == null || reply.trim().isEmpty())
            {
                reply = "抱歉，我暂时无法理解您的问题，请换个方式提问。";
            }

            log.info("AI助手回复成功，回复长度：{}", reply.length());
            
            return reply.trim();
        }
        catch (NoApiKeyException e)
        {
            log.error("API Key未设置", e);
            throw new ServiceException("AI API Key配置错误");
        }
        catch (ApiException e)
        {
            log.error("AI API调用失败", e);
            throw new ServiceException("AI服务调用失败：" + e.getMessage());
        }
        catch (Exception e)
        {
            log.error("教学助手处理消息时发生错误", e);
            throw new ServiceException("教学助手处理失败：" + e.getMessage());
        }
    }
}
