package com.ruoyi.course.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 知识图谱对象 knowledge_graph
 * 
 * @author ruoyi
 * @date 2025-11-30
 */
public class KnowledgeGraph extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 知识图谱ID */
    private Long id;

    /** 图谱标题 */
    @Excel(name = "图谱标题")
    private String title;

    /** 图谱描述 */
    @Excel(name = "图谱描述")
    private String description;

    /** 关联课程ID */
    @Excel(name = "关联课程ID")
    private Long courseId;

    /** 图谱类型：COURSE-课程图谱，CHAPTER-章节图谱 */
    @Excel(name = "图谱类型")
    private String graphType;

    /** 图谱数据（JSON格式） */
    @Excel(name = "图谱数据")
    private String graphData;

    /** 创建者ID */
    @Excel(name = "创建者ID")
    private Long creatorId;

    /** 状态：active-活跃，archived-归档 */
    @Excel(name = "状态")
    private String status;

    /** 是否删除 */
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

    public void setCourseId(Long courseId) 
    {
        this.courseId = courseId;
    }

    public Long getCourseId() 
    {
        return courseId;
    }

    public void setGraphType(String graphType) 
    {
        this.graphType = graphType;
    }

    public String getGraphType() 
    {
        return graphType;
    }

    public void setGraphData(String graphData) 
    {
        this.graphData = graphData;
    }

    public String getGraphData() 
    {
        return graphData;
    }

    public void setCreatorId(Long creatorId) 
    {
        this.creatorId = creatorId;
    }

    public Long getCreatorId() 
    {
        return creatorId;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
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
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("title", getTitle())
            .append("description", getDescription())
            .append("courseId", getCourseId())
            .append("graphType", getGraphType())
            .append("graphData", getGraphData())
            .append("creatorId", getCreatorId())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("isDeleted", getIsDeleted())
            .append("deleteTime", getDeleteTime())
            .toString();
    }
}
