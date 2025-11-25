package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;


public class KnowledgePointRenwu3 extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 知识点ID */
    private Long id;

    /** 所属课程 */
    @Excel(name = "所属课程ID")
    private Long courseId;

    /** 知识点名称（如"二分查找"） */
    @Excel(name = "知识点名称")
    private String title;

    /** 详细解释（AI生成） */
    @Excel(name = "详细解释")
    private String description;

    /** 难度：BASIC-基础，INTERMEDIATE-中级，ADVANCED-高级 */
    @Excel(name = "难度等级")
    private String level;

    /** 创建者 user.id */
    @Excel(name = "创建者ID")
    private Long creatorUserId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 软删除标记 */
    private Integer isDeleted;

    /** 删除时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deleteTime;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setCourseId(Long courseId) 
    {
        this.courseId = courseId;
    }

    public Long getCourseId() 
    {
        return courseId;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setLevel(String level) 
    {
        this.level = level;
    }

    public String getLevel() 
    {
        return level;
    }

    public void setCreatorUserId(Long creatorUserId) 
    {
        this.creatorUserId = creatorUserId;
    }

    public Long getCreatorUserId() 
    {
        return creatorUserId;
    }

    @Override
    public Date getCreateTime() 
    {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) 
    {
        this.createTime = createTime;
    }

    @Override
    public Date getUpdateTime() 
    {
        return updateTime;
    }

    @Override
    public void setUpdateTime(Date updateTime) 
    {
        this.updateTime = updateTime;
    }

    public void setIsDeleted(Integer isDeleted) 
    {
        this.isDeleted = isDeleted;
    }

    public Integer getIsDeleted() 
    {
        return isDeleted;
    }

    public void setDeleteTime(Date deleteTime) 
    {
        this.deleteTime = deleteTime;
    }

    public Date getDeleteTime() 
    {
        return deleteTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("courseId", getCourseId())
            .append("title", getTitle())
            .append("description", getDescription())
            .append("level", getLevel())
            .append("creatorUserId", getCreatorUserId())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("isDeleted", getIsDeleted())
            .append("deleteTime", getDeleteTime())
            .toString();
    }
}
