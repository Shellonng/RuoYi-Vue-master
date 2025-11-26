package com.ruoyi.system.domain;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 视频表 video
 *
 * @author ruoyi
 */
public class Video extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 视频ID */
    private Long id;

    /** 课程ID */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /** 视频标题 */
    @NotBlank(message = "视频标题不能为空")
    @Size(max = 200, message = "视频标题长度不能超过200个字符")
    private String title;

    /** 视频描述 */
    private String description;

    /** 视频文件路径 */
    @NotBlank(message = "视频文件路径不能为空")
    @Size(max = 500, message = "视频文件路径长度不能超过500个字符")
    private String filePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 时长（秒） */
    private Integer duration;

    /** 封面图片路径 */
    @Size(max = 500, message = "封面图片路径长度不能超过500个字符")
    private String coverImage;

    /** 分辨率（如：1080p） */
    @Size(max = 20, message = "分辨率长度不能超过20个字符")
    private String resolution;

    /** 关联的知识点ID列表 */
    private String knowledgePointIds;

    /** 状态 */
    private String status;

    /** 观看次数 */
    private Integer viewCount;

    /** 上传者ID */
    private Long uploadedBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getKnowledgePointIds() {
        return knowledgePointIds;
    }

    public void setKnowledgePointIds(String knowledgePointIds) {
        this.knowledgePointIds = knowledgePointIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Long uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("courseId", getCourseId())
            .append("title", getTitle())
            .append("description", getDescription())
            .append("filePath", getFilePath())
            .append("fileSize", getFileSize())
            .append("duration", getDuration())
            .append("coverImage", getCoverImage())
            .append("resolution", getResolution())
            .append("knowledgePointIds", getKnowledgePointIds())
            .append("status", getStatus())
            .append("viewCount", getViewCount())
            .append("uploadedBy", getUploadedBy())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
