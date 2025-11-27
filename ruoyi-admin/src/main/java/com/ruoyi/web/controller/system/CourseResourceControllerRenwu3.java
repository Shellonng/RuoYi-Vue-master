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
import com.ruoyi.system.utils.BusinessUserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
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
            resource.setUploadUserId(BusinessUserUtils.getCurrentBusinessUserId());
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

    /**
     * 仅分析文件并推荐知识点，不保存资源到数据库
     * 用于智能分析阶段，真正保存在用户点击"保存"按钮时进行
     */
    @Log(title = "分析文件-任务3", businessType = BusinessType.OTHER)
    @PostMapping("/analyzeOnly")
    public AjaxResult analyzeOnly(
        @RequestParam(value = "file", required = false) MultipartFile file,
        @RequestParam(value = "filePath", required = false) String existingFilePath,
        @RequestParam("courseId") Long courseId,
        @RequestParam("courseTitle") String courseTitle,
        @RequestParam(value = "description", required = false) String description
    )
    {
        try
        {
            File uploadedFile;
            String fileName;
            String fileType;
            Long fileSize;
            String tempFilePath;
            
            // 判断是上传新文件还是使用已有文件
            if (existingFilePath != null && !existingFilePath.isEmpty())
            {
                // 使用已有文件路径
                logger.info("【任务3】使用已有文件分析: {}", existingFilePath);
                
                String uploadPath = RuoYiConfig.getProfile();
                String relativePath = existingFilePath;
                
                // 处理路径前缀
                if (relativePath.startsWith("/profile/")) {
                    relativePath = relativePath.substring("/profile/".length());
                } else if (relativePath.startsWith("\\profile\\")) {
                    relativePath = relativePath.substring("\\profile\\".length());
                }
                
                uploadedFile = new File(uploadPath, relativePath);
                
                if (!uploadedFile.exists())
                {
                    return error("文件不存在: " + uploadedFile.getAbsolutePath());
                }
                
                fileName = uploadedFile.getName();
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                fileType = extension.toLowerCase();
                fileSize = uploadedFile.length();
                tempFilePath = existingFilePath;
            }
            else if (file != null && !file.isEmpty())
            {
                // 上传新文件
                fileName = file.getOriginalFilename();
                String extension = FileUploadUtils.getExtension(file);
                fileType = extension.toLowerCase();
                fileSize = file.getSize();
                
                // 临时保存文件用于分析
                tempFilePath = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file);
                String uploadPath = RuoYiConfig.getProfile();
                String relativePath = tempFilePath;
                if (relativePath.startsWith("/profile/")) {
                    relativePath = relativePath.substring("/profile/".length());
                } else if (relativePath.startsWith("\\profile\\")) {
                    relativePath = relativePath.substring("\\profile\\".length());
                }
                uploadedFile = new File(uploadPath, relativePath);
            }
            else
            {
                return error("请提供文件或文件路径");
            }

            // 验证文件类型
            boolean isDocument = fileType.equals("pdf") || fileType.equals("doc") || fileType.equals("docx");
            boolean isVideo = fileType.equals("mp4") || fileType.equals("avi") || fileType.equals("mov") 
                || fileType.equals("wmv") || fileType.equals("flv") || fileType.equals("mkv");
            
            if (!isDocument && !isVideo)
            {
                return error("只支持PDF、Word和视频格式的文件");
            }
            
            logger.info("【任务3】分析文档: {}", uploadedFile.getAbsolutePath());
            
            // 调用分析服务
            Map<String, Object> analysisResult = resourceTaggingService
                .analyzeAndRecommendKnowledgePoints(uploadedFile, fileType, courseId, courseTitle);
            
            List<Map<String, Object>> recommendations = (List<Map<String, Object>>) analysisResult.get("recommendations");
            String extractedText = (String) analysisResult.get("extractedText");
            Integer textLength = (Integer) analysisResult.get("textLength");
            
            // 返回分析结果，包含临时文件信息（不保存到数据库）
            AjaxResult result = success("文件分析完成");
            
            // 构建临时资源信息
            Map<String, Object> tempResource = new HashMap<>();
            tempResource.put("fileName", fileName);
            tempResource.put("fileType", fileType);
            tempResource.put("fileSize", fileSize);
            tempResource.put("filePath", tempFilePath);
            tempResource.put("description", description != null ? description : "");
            
            result.put("tempResource", tempResource);
            result.put("recommendations", recommendations);
            result.put("recommendationCount", recommendations.size());
            result.put("extractedText", extractedText);
            result.put("textLength", textLength);
            
            logger.info("【任务3】分析完成，推荐了{}个知识点", recommendations.size());
            
            return result;
        }
        catch (Exception e)
        {
            logger.error("【任务3】文件分析失败", e);
            return error("文件分析失败: " + e.getMessage());
        }
    }

    /**
     * 保存分析后的资源到数据库
     */
    @Log(title = "保存资源-任务3", businessType = BusinessType.INSERT)
    @PostMapping("/saveResource")
    public AjaxResult saveResource(@RequestBody Map<String, Object> params)
    {
        try
        {
            Long courseId = Long.valueOf(params.get("courseId").toString());
            String fileName = params.get("fileName").toString();
            String fileType = params.get("fileType").toString();
            Long fileSize = Long.valueOf(params.get("fileSize").toString());
            String filePath = params.get("filePath").toString();
            String description = params.containsKey("description") ? params.get("description").toString() : "";
            
            // 创建课程资源记录
            CourseResourceRenwu3 resource = new CourseResourceRenwu3();
            resource.setCourseId(courseId);
            resource.setName(fileName);
            resource.setFileType(fileType);
            resource.setFileSize(fileSize);
            resource.setFileUrl(filePath);
            resource.setDescription(description);
            resource.setDownloadCount(0);
            resource.setUploadUserId(BusinessUserUtils.getCurrentBusinessUserId());
            resource.setCreateTime(new Date());
            resource.setUpdateTime(new Date());
            
            courseResourceService.insertCourseResource(resource);
            
            logger.info("【任务3】资源保存成功: {}", resource.getId());
            
            AjaxResult result = success("资源保存成功");
            result.put("resource", resource);
            return result;
        }
        catch (Exception e)
        {
            logger.error("【任务3】资源保存失败", e);
            return error("资源保存失败: " + e.getMessage());
        }
    }

    @Log(title = "确认资源知识点-任务3", businessType = BusinessType.UPDATE)
    @PostMapping("/confirmKnowledgePoints")
    public AjaxResult confirmKnowledgePoints(@RequestBody Map<String, Object> params)
    {
        Long resourceId = Long.valueOf(params.get("resourceId").toString());
        @SuppressWarnings("unchecked")
        List<Integer> kpIdsInt = (List<Integer>) params.get("kpIds");
        List<Long> kpIds = kpIdsInt.stream().map(Long::valueOf).collect(java.util.stream.Collectors.toList());
        
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

    /**
     * 创建新知识点并关联到资源
     */
    @Log(title = "创建知识点-任务3", businessType = BusinessType.INSERT)
    @PostMapping("/createKnowledgePoint")
    public AjaxResult createKnowledgePoint(@RequestBody Map<String, Object> params)
    {
        Long resourceId = Long.valueOf(params.get("resourceId").toString());
        Long courseId = Long.valueOf(params.get("courseId").toString());
        String kpTitle = params.get("kpTitle").toString();
        
        logger.info("【任务3】创建新知识点: resourceId={}, courseId={}, title={}", resourceId, courseId, kpTitle);
        
        Long creatorUserId = BusinessUserUtils.getCurrentBusinessUserId();
        Long kpId = resourceTaggingService.createAndLinkKnowledgePoint(resourceId, courseId, kpTitle, creatorUserId);
        
        if (kpId != null)
        {
            AjaxResult result = success("知识点创建成功");
            result.put("kpId", kpId);
            return result;
        }
        else
        {
            return error("知识点创建失败");
        }
    }

    /**
     * 批量创建知识点并关联到资源
     */
    @Log(title = "批量创建知识点-任务3", businessType = BusinessType.INSERT)
    @PostMapping("/batchCreateKnowledgePoints")
    public AjaxResult batchCreateKnowledgePoints(@RequestBody Map<String, Object> params)
    {
        Long resourceId = Long.valueOf(params.get("resourceId").toString());
        Long courseId = Long.valueOf(params.get("courseId").toString());
        @SuppressWarnings("unchecked")
        List<String> kpTitles = (List<String>) params.get("kpTitles");
        
        logger.info("【任务3】批量创建知识点: resourceId={}, courseId={}, count={}", resourceId, courseId, kpTitles.size());
        
        Long creatorUserId = BusinessUserUtils.getCurrentBusinessUserId();
        List<Long> createdIds = resourceTaggingService.batchCreateAndLinkKnowledgePoints(resourceId, courseId, kpTitles, creatorUserId);
        
        AjaxResult result = success("成功创建" + createdIds.size() + "个知识点");
        result.put("createdIds", createdIds);
        result.put("successCount", createdIds.size());
        result.put("totalCount", kpTitles.size());
        return result;
    }

    @Log(title = "课程资源-任务3", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CourseResourceRenwu3 courseResource)
    {
        courseResource.setUploadUserId(BusinessUserUtils.getCurrentBusinessUserId());
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

    /**
     * 下载课程资源文件
     */
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response)
    {
        try
        {
            CourseResourceRenwu3 resource = courseResourceService.selectCourseResourceById(id);
            if (resource == null || resource.getFileUrl() == null)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 获取文件路径（去掉可能的URL前缀）
            String fileUrl = resource.getFileUrl();
            String filePath = fileUrl;
            
            // 如果是相对路径，则添加上传路径前缀
            if (!fileUrl.startsWith("http") && !fileUrl.startsWith("/"))
            {
                filePath = RuoYiConfig.getProfile() + "/" + fileUrl;
            }
            else if (fileUrl.startsWith("/profile"))
            {
                filePath = RuoYiConfig.getProfile() + fileUrl.substring(8);
            }

            File file = new File(filePath);
            if (!file.exists())
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 获取文件名
            String fileName = resource.getFileName();
            if (fileName == null || fileName.isEmpty())
            {
                fileName = resource.getName();
            }
            if (fileName == null || fileName.isEmpty())
            {
                fileName = file.getName();
            }

            // 设置响应头
            response.setContentType(getContentType(resource.getFileType()));
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setContentLengthLong(file.length());

            // 读取文件并写入响应流
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream())
            {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1)
                {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        }
        catch (Exception e)
        {
            logger.error("文件下载失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据文件类型获取Content-Type
     */
    private String getContentType(String fileType)
    {
        if (fileType == null) return "application/octet-stream";
        
        fileType = fileType.toLowerCase();
        switch (fileType)
        {
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/x-ms-wmv";
            case "flv":
                return "video/x-flv";
            case "mkv":
                return "video/x-matroska";
            default:
                return "application/octet-stream";
        }
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
