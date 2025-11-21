package com.ruoyi.web.controller.system;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
}
