package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;


public class CourseResourceRenwu3 extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 资源ID */
    private Long id;

    /** 所属课程ID */
    @Excel(name = "所属课程ID")
    private Long courseId;

    /** 资源名称 */
    @Excel(name = "资源名称")
    private String name;

    /** 文件类型，如pdf、doc、ppt等 */
    @Excel(name = "文件类型")
    private String fileType;

    /** 文件大小(字节) */
    @Excel(name = "文件大小(字节)")
    private Long fileSize;

    /** 文件URL */
    @Excel(name = "文件URL")
    private String fileUrl;

    /** 资源描述 */
    @Excel(name = "资源描述")
    private String description;

    /** 下载次数 */
    @Excel(name = "下载次数")
    private Integer downloadCount;

    /** 上传用户ID */
    @Excel(name = "上传用户ID")
    private Long uploadUserId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 课程名称（关联查询，不在数据库中） */
    private String courseName;

    /** 文件名称（从course_resource.name或文件路径提取） */
    private String fileName;

    /** 关联的知识点列表 */
    private List<KnowledgePoint> knowledgePointList;

    /** 知识点名称（用于搜索，不在数据库中） */
    private String knowledgePointName;

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
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setFileType(String fileType) 
    {
        this.fileType = fileType;
    }

    public String getFileType() 
    {
        return fileType;
    }
    public void setFileSize(Long fileSize) 
    {
        this.fileSize = fileSize;
    }

    public Long getFileSize() 
    {
        return fileSize;
    }
    public void setFileUrl(String fileUrl) 
    {
        this.fileUrl = fileUrl;
    }

    public String getFileUrl() 
    {
        return fileUrl;
    }
    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }
    public void setDownloadCount(Integer downloadCount) 
    {
        this.downloadCount = downloadCount;
    }

    public Integer getDownloadCount() 
    {
        return downloadCount;
    }
    public void setUploadUserId(Long uploadUserId) 
    {
        this.uploadUserId = uploadUserId;
    }

    public Long getUploadUserId() 
    {
        return uploadUserId;
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

    public String getCourseName() 
    {
        return courseName;
    }

    public void setCourseName(String courseName) 
    {
        this.courseName = courseName;
    }

    public String getFileName() 
    {
        return fileName;
    }

    public void setFileName(String fileName) 
    {
        this.fileName = fileName;
    }

    public List<KnowledgePoint> getKnowledgePointList()
    {
        return knowledgePointList;
    }

    public void setKnowledgePointList(List<KnowledgePoint> knowledgePointList)
    {
        this.knowledgePointList = knowledgePointList;
    }

    public String getKnowledgePointName()
    {
        return knowledgePointName;
    }

    public void setKnowledgePointName(String knowledgePointName)
    {
        this.knowledgePointName = knowledgePointName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("courseId", getCourseId())
            .append("name", getName())
            .append("fileType", getFileType())
            .append("fileSize", getFileSize())
            .append("fileUrl", getFileUrl())
            .append("description", getDescription())
            .append("downloadCount", getDownloadCount())
            .append("uploadUserId", getUploadUserId())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
