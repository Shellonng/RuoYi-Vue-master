package com.exampl.smartcourse.controller.smartpaper;

import com.exampl.smartcourse.common.smartpaper.Result;
import com.exampl.smartcourse.dto.smartpaper.response.FileUploadResponse;
import com.exampl.smartcourse.service.smartpaper.IFileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件管理控制器
 */
@Tag(name = "文件管理", description = "文件上传、下载相关接口")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final IFileService fileService;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Operation(
        summary = "上传文件",
        description = "支持上传文档、图片等文件，可指定分类(answer/material/attachment)"
    )
    @PostMapping(
        value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Result<FileUploadResponse> uploadFile(
            @Parameter(
                description = "上传的文件",
                required = true,
                content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestPart("file") MultipartFile file,
            
            @Parameter(
                description = "文件分类: answer(答案), material(资料), attachment(附件)",
                example = "answer",
                schema = @Schema(type = "string", allowableValues = {"answer", "material", "attachment"})
            )
            @RequestParam(value = "category", required = false, defaultValue = "answer") String category) {

        log.info("收到文件上传请求：文件名={}, 大小={}, 分类={}",
                file.getOriginalFilename(), file.getSize(), category);

        FileUploadResponse response = fileService.uploadFile(file, category);
        return Result.success("文件上传成功", response);
    }

    @Operation(
        summary = "上传课程资源文件",
        description = "将文件保存到 /courses/{courseId}/{uploaderUserId}/，返回可下载的相对URL"
    )
    @PostMapping(
        value = "/upload-to-courses",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Result<FileUploadResponse> uploadToCourses(
            @Parameter(description = "上传的文件", required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
            @RequestPart("file") MultipartFile file,

            @Parameter(description = "课程ID", required = true)
            @RequestParam("courseId") Long courseId,

            @Parameter(description = "上传者用户ID", required = true)
            @RequestParam("uploaderUserId") Long uploaderUserId
    ) {
        log.info("收到课程资源上传请求：file={}, courseId={}, uploaderUserId={}",
                file != null ? file.getOriginalFilename() : null, courseId, uploaderUserId);
        FileUploadResponse response = fileService.uploadToCourses(file, courseId, uploaderUserId);
        return Result.success("文件上传成功", response);
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download/**")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "文件路径") @RequestParam("filePath") String filePath) {

        try {
            // 构造完整文件路径
            String fullPath = uploadPath + filePath.replace("/uploads", "");
            File file = new File(fullPath);

            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            String fileName = file.getName();
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                    .body(resource);
        } catch (Exception e) {
            log.error("文件下载失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "删除文件")
    @DeleteMapping
    public Result<Void> deleteFile(
            @Parameter(description = "文件URL") @RequestParam("fileUrl") String fileUrl) {

        log.info("收到文件删除请求：fileUrl={}", fileUrl);
        fileService.deleteFile(fileUrl);
        return Result.success("文件删除成功", null);
    }
}
