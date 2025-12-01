package com.exampl.smartcourse.service.smartpaper;

import com.exampl.smartcourse.dto.smartpaper.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface IFileService {
    /**
     * 上传文件
     * @param file 文件
     * @param category 文件分类（如：assignment, answer等）
     * @return 文件上传响应
     */
    FileUploadResponse uploadFile(MultipartFile file, String category);

    /**
     * 删除文件
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);

    /**
     * 验证文件类型
     * @param fileName 文件名
     * @return 是否允许上传
     */
    boolean validateFileType(String fileName);

    /**
     * 上传文件到课程资源目录 /courses/{courseId}/{uploaderUserId}/
     * @param file 文件
     * @param courseId 课程ID
     * @param uploaderUserId 上传者用户ID
     * @return 文件上传响应，fileUrl 形如 /courses/{courseId}/{uploaderUserId}/{originalFilename}
     */
    FileUploadResponse uploadToCourses(MultipartFile file, Long courseId, Long uploaderUserId);
}
