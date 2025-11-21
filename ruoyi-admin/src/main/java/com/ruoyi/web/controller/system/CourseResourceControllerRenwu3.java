package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.system.domain.CourseResourceRenwu3;
import com.ruoyi.system.domain.KnowledgePointRenwu3;
import com.ruoyi.system.service.ICourseResourceServiceRenwu3;
import com.ruoyi.system.service.IResourceTaggingServiceRenwu3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/resource/renwu3")
public class CourseResourceControllerRenwu3 extends BaseController
{
    @Autowired
    private ICourseResourceServiceRenwu3 courseResourceService;

    @Autowired
    private IResourceTaggingServiceRenwu3 resourceTaggingService;

    @GetMapping("/list")
    public TableDataInfo list(CourseResourceRenwu3 courseResource)
    {
        startPage();
        List<CourseResourceRenwu3> list = courseResourceService.selectCourseResourceList(courseResource);
        return getDataTable(list);
    }

    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(courseResourceService.selectCourseResourceById(id));
    }

    /**
     * 上传课程资源并智能推荐知识点
     * 
     * 这是任务3的核心功能：
     * 1. 接收PDF/Word文件上传
     * 2. 调用文档解析工具提取文本
     * 3. 调用DeepSeek API分析并提取知识点
     * 4. 与数据库现有知识点匹配
     * 5. 返回推荐结果给前端
     * 
     * @param file 上传的文件
     * @param courseId 课程ID
     * @param courseTitle 课程名称
     * @param description 资源描述
     * @return 上传结果及知识点推荐
     */
    @Log(title = "课程资源上传-任务3", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult uploadAndAnalyze(
        @RequestParam("file") MultipartFile file,
        @RequestParam("courseId") Long courseId,
        @RequestParam("courseTitle") String courseTitle,
        @RequestParam(value = "description", required = false) String description
    )
    {
        try
        {
            if (file.isEmpty())
            {
                return error("上传文件不能为空");
            }

            // 获取文件名和扩展名
            String fileName = file.getOriginalFilename();
            String extension = FileUploadUtils.getExtension(file);
            String fileType = extension.toLowerCase();

            // 验证文件类型（任务3要求：支持PDF、Word和视频）
            boolean isDocument = fileType.equals("pdf") || fileType.equals("doc") || fileType.equals("docx");
            boolean isVideo = fileType.equals("mp4") || fileType.equals("avi") || fileType.equals("mov") 
                || fileType.equals("wmv") || fileType.equals("flv") || fileType.equals("mkv");
            
            if (!isDocument && !isVideo)
            {
                return error("只支持PDF、Word和视频格式的文件（.pdf, .doc, .docx, .mp4, .avi, .mov等）");
            }

            // 保存文件
            String filePath = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file);
            
            // 创建课程资源记录
            CourseResourceRenwu3 resource = new CourseResourceRenwu3();
            resource.setCourseId(courseId);
            resource.setName(fileName);
            resource.setFileType(fileType);
            resource.setFileSize(file.getSize());
            resource.setFileUrl(filePath);
            resource.setDescription(description);
            resource.setDownloadCount(0);
            resource.setUploadUserId(SecurityUtils.getUserId());
            resource.setCreateTime(new Date());
            resource.setUpdateTime(new Date());
            
            courseResourceService.insertCourseResource(resource);
            
            // 【任务3核心】智能分析:提取知识点推荐
            String uploadPath = RuoYiConfig.getProfile();
            // 处理文件路径:去掉/profile前缀,因为实际文件保存在uploadPath下
            // 例如: /profile/upload/xxx.docx -> upload/xxx.docx
            String relativePath = filePath;
            if (relativePath.startsWith("/profile/")) {
                relativePath = relativePath.substring("/profile/".length());
            } else if (relativePath.startsWith("\\profile\\")) {
                relativePath = relativePath.substring("\\profile\\".length());
            }
            File uploadedFile = new File(uploadPath, relativePath);
            
            logger.info("【任务3】开始智能分析文档: {}", uploadedFile.getAbsolutePath());
            
            Map<String, Object> analysisResult = resourceTaggingService
                .analyzeAndRecommendKnowledgePoints(uploadedFile, fileType, courseId, courseTitle);
            
            List<Map<String, Object>> recommendations = (List<Map<String, Object>>) analysisResult.get("recommendations");
            String extractedText = (String) analysisResult.get("extractedText");
            Integer textLength = (Integer) analysisResult.get("textLength");
            
            // 返回资源信息和知识点推荐
            AjaxResult result = success("文件上传成功，AI智能分析完成");
            result.put("resource", resource);
            result.put("recommendations", recommendations);
            result.put("recommendationCount", recommendations.size());
            result.put("extractedText", extractedText);
            result.put("textLength", textLength);
            
            logger.info("【任务3】智能打标完成，推荐了{}个知识点", recommendations.size());
            
            return result;
        }
        catch (Exception e)
        {
            logger.error("【任务3】文件上传失败", e);
            return error("文件上传失败: " + e.getMessage());
        }
    }

    @Log(title = "确认资源知识点-任务3", businessType = BusinessType.UPDATE)
    @PostMapping("/confirmKnowledgePoints")
    public AjaxResult confirmKnowledgePoints(
        @RequestParam("resourceId") Long resourceId,
        @RequestParam("kpIds") List<Long> kpIds
    )
    {
        logger.info("【任务3】确认资源{}的知识点: {}", resourceId, kpIds);
        boolean success = resourceTaggingService.confirmResourceKnowledgePoints(resourceId, kpIds);
        return success ? success("知识点关联成功") : error("保存失败");
    }

    @GetMapping("/knowledgePoints/{resourceId}")
    public AjaxResult getResourceKnowledgePoints(@PathVariable("resourceId") Long resourceId)
    {
        List<KnowledgePointRenwu3> knowledgePoints = resourceTaggingService.getResourceKnowledgePoints(resourceId);
        return success(knowledgePoints);
    }

    @Log(title = "课程资源-任务3", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CourseResourceRenwu3 courseResource)
    {
        courseResource.setUploadUserId(SecurityUtils.getUserId());
        return toAjax(courseResourceService.insertCourseResource(courseResource));
    }

    @Log(title = "课程资源-任务3", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CourseResourceRenwu3 courseResource)
    {
        return toAjax(courseResourceService.updateCourseResource(courseResource));
    }

    /**
     * 删除课程资源
     */
    @Log(title = "课程资源-任务3", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(courseResourceService.deleteCourseResourceByIds(ids));
    }

    @PostMapping("/chat")
    public AjaxResult chatWithAI(
        @RequestParam("message") String userMessage,
        @RequestParam(value = "resourceId", required = false) Long resourceId,
        @RequestParam(value = "courseTitle", required = false) String courseTitle,
        @RequestParam(value = "recommendationCount", required = false, defaultValue = "0") Integer recommendationCount,
        @RequestParam(value = "matchedCount", required = false, defaultValue = "0") Integer matchedCount
    )
    {
        try
        {
            // 构建上下文信息
            StringBuilder contextInfo = new StringBuilder();
            if (courseTitle != null)
            {
                contextInfo.append("课程名称: ").append(courseTitle).append("\n");
            }
            contextInfo.append("总共提取知识点数: ").append(recommendationCount).append("\n");
            contextInfo.append("已匹配知识点数: ").append(matchedCount).append("\n");
            contextInfo.append("未匹配知识点数: ").append(recommendationCount - matchedCount).append("\n");
            contextInfo.append("匹配阈值: 60%\n");
            contextInfo.append("使用的匹配算法: Levenshtein编辑距离算法");
            
            // 调用AI对话
            String aiResponse = com.ruoyi.common.utils.ai.DeepSeekClientRenwu3.chatWithAI(
                userMessage, 
                contextInfo.toString()
            );
            
            return success(aiResponse);
        }
        catch (Exception e)
        {
            logger.error("AI对话失败", e);
            return error("对话失败: " + e.getMessage());
        }
    }
}
