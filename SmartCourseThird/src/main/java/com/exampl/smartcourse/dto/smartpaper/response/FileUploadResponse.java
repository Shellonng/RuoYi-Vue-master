package com.exampl.smartcourse.dto.smartpaper.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 文件上传响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传响应")
public class FileUploadResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件访问URL")
    private String fileUrl;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "上传时间")
    private String uploadTime;
}
