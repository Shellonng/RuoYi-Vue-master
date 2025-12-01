package com.exampl.smartcourse.service.smartpaper.impl;

import com.exampl.smartcourse.dto.smartpaper.response.FileUploadResponse;
import com.exampl.smartcourse.exception.smartpaper.BusinessException;
import com.exampl.smartcourse.service.smartpaper.IFileService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务实现类
 */
@Service
@Slf4j
public class FileServiceImpl implements IFileService {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.allowed-extensions}")
    private String allowedExtensions;

    @Value("${smartcourse.file.storage.mode:local}")
    private String storageMode;

    @Value("${smartcourse.file.storage.local-base:}")
    private String localBase;

    @Value("${smartcourse.file.storage.server-base:}")
    private String serverBase;

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(400, "文件名不能为空");
        }

        // 验证文件类型
        if (!validateFileType(originalFilename)) {
            throw new BusinessException(400, "不支持的文件类型，仅支持：" + allowedExtensions);
        }

        try {
            // 创建上传目录
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String categoryPath = category != null ? category : "default";
            String fullPath = uploadPath + "/" + categoryPath + "/" + datePath;

            File directory = new File(fullPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 生成唯一文件名
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            String filePath = fullPath + "/" + newFileName;

            // 保存文件
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());

            // 构造访问URL（相对路径）
            String fileUrl = "/uploads/" + categoryPath + "/" + datePath + "/" + newFileName;

            log.info("文件上传成功：原文件名={}, 新文件名={}, 路径={}", originalFilename, newFileName, filePath);

            return new FileUploadResponse(
                    originalFilename,
                    fileUrl,
                    file.getSize(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(500, "文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public FileUploadResponse uploadToCourses(MultipartFile file, Long courseId, Long uploaderUserId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        if (courseId == null || uploaderUserId == null) {
            throw new BusinessException(400, "缺少课程ID或上传者ID");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException(400, "文件名不能为空");
        }
        if (!validateFileType(originalFilename)) {
            throw new BusinessException(400, "不支持的文件类型，仅支持：" + allowedExtensions);
        }
        try {
            String base = "server".equalsIgnoreCase(storageMode) ? serverBase : localBase;
            if (base == null || base.isEmpty()) {
                throw new BusinessException(500, "文件存储根路径未配置");
            }
            String normalized = base.replace('\\', '/');
            if (!normalized.endsWith("/")) normalized += "/";
            String dir = normalized + "courses/" + courseId + "/" + uploaderUserId + "/";
            File directory = new File(dir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String filePath = dir + originalFilename;
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());

            String fileUrl = "/courses/" + courseId + "/" + uploaderUserId + "/" + originalFilename;
            log.info("课程资源上传成功：课程ID={}, 上传者ID={}, 文件名={}, 路径={}", courseId, uploaderUserId, originalFilename, filePath);
            return new FileUploadResponse(
                    originalFilename,
                    fileUrl,
                    file.getSize(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
        } catch (IOException e) {
            log.error("课程资源上传失败", e);
            throw new BusinessException(500, "文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // 将URL转换为实际文件路径
            String filePath = uploadPath + fileUrl.replace("/uploads", "");
            File file = new File(filePath);

            if (file.exists() && file.isFile()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("文件删除成功：{}", filePath);
                } else {
                    log.warn("文件删除失败：{}", filePath);
                }
            }
        } catch (Exception e) {
            log.error("文件删除异常", e);
        }
    }

    @Override
    public boolean validateFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return false;
        }

        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        List<String> allowedList = Arrays.asList(allowedExtensions.split(","));

        return allowedList.contains(extension);
    }
}
