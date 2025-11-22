package com.ruoyi.system.domain;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 课程小节表 section
 *
 * @author ruoyi
 */
public class Section extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 小节ID */
    private Long id;

    /** 所属章节ID */
    private Long chapterId;

    /** 小节名称 */
    @NotBlank(message = "小节名称不能为空")
    @Size(min = 1, max = 255, message = "小节名称长度不能超过255个字符")
    private String title;

    /** 小节简介 */
    private String description;

    /** 视频播放地址 */
    private String videoUrl;

    /** 视频时长（秒） */
    private Integer duration;

    /** 排序顺序 */
    private Integer sortOrder;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 是否删除 */
    private Integer isDeleted;

    /** 删除时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deleteTime;

    /** 小节类型（用于前端显示） */
    private String type;

    /** 小节完成状态（用于前端显示） */
    private Boolean completed;

    /** 章节名称（用于前端显示，关联查询） */
    private String chapterName;

    /** 课程ID（用于前端显示，关联查询） */
    private Long courseId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
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

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("chapterId", getChapterId())
            .append("title", getTitle())
            .append("description", getDescription())
            .append("videoUrl", getVideoUrl())
            .append("duration", getDuration())
            .append("sortOrder", getSortOrder())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("isDeleted", getIsDeleted())
            .append("deleteTime", getDeleteTime())
            .toString();
    }
}
