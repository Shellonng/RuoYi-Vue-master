package com.ruoyi.web.controller.system;

import java.io.File;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.system.domain.dto.CourseStructureDTO;
import com.ruoyi.system.service.AIService;
import com.ruoyi.system.service.CourseGenerationService;
import com.ruoyi.system.utils.BusinessUserUtils;

/**
 * AI课程生成 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/course/generation")
public class CourseGenerationController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(CourseGenerationController.class);

    @Autowired
    private AIService aiService;

    @Autowired
    private CourseGenerationService courseGenerationService;

    /**
     * 上传文档并生成课程结构
     *
     * @param file 文档文件（支持PDF、Word、PPT）
     * @param courseId 课程ID
     * @param courseName 课程名称
     * @return 生成结果
     */
    @Log(title = "AI生成课程结构", businessType = BusinessType.INSERT)
    @PostMapping("/uploadAndGenerate/{courseId}")
    public AjaxResult uploadAndGenerate(
        @RequestParam("file") MultipartFile file,
        @PathVariable Long courseId,
        @RequestParam("courseName") String courseName)
    {
        try
        {
            // 1. 验证文件
            if (file.isEmpty())
            {
                return error("请上传文档文件");
            }

            String fileName = file.getOriginalFilename();
            if (!isValidFileType(fileName))
            {
                return error("只支持PDF、Word（.doc/.docx）、PowerPoint（.ppt/.pptx）格式文件");
            }

            log.info("开始处理文档文件：{}，课程ID：{}，课程名称：{}", fileName, courseId, courseName);

            // 2. 保存上传的文件到临时目录
            String uploadPath = RuoYiConfig.getUploadPath();
            
            // 创建临时文件用于AI处理
            File tempDir = new File(uploadPath, "temp");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            
            String tempFileName = System.currentTimeMillis() + "_" + fileName;
            File documentFile = new File(tempDir, tempFileName);
            file.transferTo(documentFile);
            
            log.info("文件上传成功，临时保存路径：{}", documentFile.getAbsolutePath());

            try
            {
                // 3. 提取文档文本
                log.info("开始提取文档文本");
                String documentText = aiService.extractTextFromFile(documentFile);
                log.info("文档文本提取完成，文本长度：{}，前100字符：{}", 
                    documentText.length(), 
                    documentText.length() > 100 ? documentText.substring(0, 100) : documentText);

                if (documentText == null || documentText.trim().isEmpty())
                {
                    return error("文档内容为空，无法生成课程结构");
                }

                // 4. 调用AI生成课程结构
                log.info("开始调用AI生成课程结构，课程名称：{}", courseName);
                CourseStructureDTO structure = aiService.generateCourseStructure(documentText, courseName);
                
                if (structure == null || structure.getChapters() == null || structure.getChapters().isEmpty())
                {
                    log.error("AI生成的课程结构为空");
                    return error("AI生成的课程结构为空，请检查文档内容或重试");
                }
                
                log.info("AI生成课程结构完成,章节数:{}", structure.getChapters().size());

                // 5. 保存到数据库
                Long currentUserId = BusinessUserUtils.getCurrentBusinessUserId();
                log.info("当前业务用户ID: {}", currentUserId);
                String result = courseGenerationService.saveCourseStructure(courseId, structure, currentUserId);
                log.info("课程结构保存完成：{}", result);

                // 6. 返回结果
                AjaxResult ajax = AjaxResult.success(result);
                ajax.put("structure", structure);
                ajax.put("message", result);
                ajax.put("chapterCount", structure.getChapters().size());
                
                return ajax;
            }
            finally
            {
                // 7. 清理临时文件
                try
                {
                    FileUtils.deleteFile(documentFile.getAbsolutePath());
                    log.info("已清理临时文件：{}", documentFile.getAbsolutePath());
                }
                catch (Exception e)
                {
                    log.warn("清理临时文件失败：{}", e.getMessage());
                }
            }
        }
        catch (Exception e)
        {
            log.error("生成课程结构失败", e);
            return error("生成课程结构失败：" + e.getMessage());
        }
    }

    /**
     * 仅解析文档（不保存到数据库）
     *
     * @param file 文档文件（支持PDF、Word、PPT）
     * @param courseName 课程名称
     * @return 课程结构预览
     */
    @PostMapping("/parseOnly")
    public AjaxResult parseOnly(
        @RequestParam("file") MultipartFile file,
        @RequestParam("courseName") String courseName)
    {
        try
        {
            // 验证文件
            if (file.isEmpty())
            {
                return error("请上传文档文件");
            }

            String fileName = file.getOriginalFilename();
            if (!isValidFileType(fileName))
            {
                return error("只支持PDF、Word（.doc/.docx）、PowerPoint（.ppt/.pptx）格式文件");
            }

            log.info("开始解析文档文件：{}，课程名称：{}", fileName, courseName);

            // 保存临时文件
            String uploadPath = RuoYiConfig.getUploadPath();
            String filePath = FileUploadUtils.upload(uploadPath, file);
            File documentFile = new File(RuoYiConfig.getProfile() + filePath);

            try
            {
                // 提取文档文本
                String documentText = aiService.extractTextFromFile(documentFile);
                
                // 调用AI生成课程结构
                CourseStructureDTO structure = aiService.generateCourseStructure(documentText, courseName);

                // 返回结构预览
                AjaxResult ajax = AjaxResult.success("解析成功");
                ajax.put("structure", structure);
                ajax.put("chapterCount", structure.getChapters().size());
                
                int sectionCount = structure.getChapters().stream()
                    .mapToInt(c -> c.getSections() != null ? c.getSections().size() : 0)
                    .sum();
                ajax.put("sectionCount", sectionCount);
                
                int kpCount = structure.getChapters().stream()
                    .flatMap(c -> c.getSections() != null ? c.getSections().stream() : java.util.stream.Stream.empty())
                    .mapToInt(s -> s.getKnowledgePoints() != null ? s.getKnowledgePoints().size() : 0)
                    .sum();
                ajax.put("knowledgePointCount", kpCount);
                
                return ajax;
            }
            finally
            {
                // 清理临时文件
                FileUtils.deleteFile(documentFile.getAbsolutePath());
            }
        }
        catch (Exception e)
        {
            log.error("解析文档失败", e);
            return error("解析文档失败：" + e.getMessage());
        }
    }

    /**
     * 验证文件类型是否支持
     */
    private boolean isValidFileType(String fileName)
    {
        if (fileName == null)
        {
            return false;
        }
        
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".pdf") 
            || lowerFileName.endsWith(".doc") 
            || lowerFileName.endsWith(".docx")
            || lowerFileName.endsWith(".ppt")
            || lowerFileName.endsWith(".pptx");
    }

    /**
     * AI生成课程描述
     *
     * @param params 包含courseTitle的参数
     * @return 生成的课程描述
     */
    @Log(title = "AI生成课程描述", businessType = BusinessType.OTHER)
    @PostMapping("/generateDescription")
    public AjaxResult generateDescription(@RequestBody Map<String, String> params)
    {
        try
        {
            String courseTitle = params.get("courseTitle");
            
            if (courseTitle == null || courseTitle.trim().isEmpty())
            {
                return error("课程标题不能为空");
            }

            log.info("开始生成课程描述，课程标题：{}", courseTitle);

            // 调用AI服务生成课程描述
            String description = aiService.generateCourseDescription(courseTitle);
            
            if (description == null || description.trim().isEmpty())
            {
                return error("生成课程描述失败，请重试");
            }

            log.info("课程描述生成成功，长度：{}", description.length());
            
            return success(description);
        }
        catch (Exception e)
        {
            log.error("生成课程描述失败", e);
            return error("生成课程描述失败：" + e.getMessage());
        }
    }

    /**
     * AI智能匹配知识点
     *
     * @param params 匹配参数，包含作业信息和可选知识点列表
     * @return 匹配的知识点ID列表
     */
    @Log(title = "AI匹配知识点", businessType = BusinessType.OTHER)
    @PostMapping("/matchKnowledgePoints")
    public AjaxResult matchKnowledgePoints(@RequestBody Map<String, Object> params)
    {
        try
        {
            String assignmentTitle = (String) params.get("assignmentTitle");
            String assignmentDescription = (String) params.get("assignmentDescription");
            
            if ((assignmentTitle == null || assignmentTitle.trim().isEmpty()) 
                && (assignmentDescription == null || assignmentDescription.trim().isEmpty()))
            {
                return error("作业标题或描述不能同时为空");
            }

            log.info("开始AI匹配知识点，作业标题：{}，描述长度：{}", 
                assignmentTitle, 
                assignmentDescription != null ? assignmentDescription.length() : 0);

            // 调用AI服务匹配知识点
            java.util.List<Long> matchedKpIds = aiService.matchKnowledgePoints(
                assignmentTitle, 
                assignmentDescription, 
                params.get("attachments"),
                params.get("availableKnowledgePoints")
            );
            
            if (matchedKpIds == null || matchedKpIds.isEmpty())
            {
                log.info("AI未匹配到合适的知识点");
                return success(new java.util.HashMap<String, Object>() {{
                    put("matchedKnowledgePointIds", new java.util.ArrayList<>());
                    put("message", "未找到匹配的知识点");
                }});
            }

            log.info("AI成功匹配{}个知识点：{}", matchedKpIds.size(), matchedKpIds);
            
            return success(new java.util.HashMap<String, Object>() {{
                put("matchedKnowledgePointIds", matchedKpIds);
                put("count", matchedKpIds.size());
                put("message", "匹配成功");
            }});
        }
        catch (Exception e)
        {
            log.error("AI匹配知识点失败", e);
            return error("AI匹配知识点失败：" + e.getMessage());
        }
    }
}
