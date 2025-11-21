package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;


public class CourseResourceKpRenwu3 extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 关联ID */
    private Long id;

    /** 资源ID（允许为空） */
    private Long resourceId;

    /** 知识点ID */
    private Long kpId;

    /** 教师是否确认 */
    private Integer isConfirmed;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setResourceId(Long resourceId) 
    {
        this.resourceId = resourceId;
    }

    public Long getResourceId() 
    {
        return resourceId;
    }

    public void setKpId(Long kpId) 
    {
        this.kpId = kpId;
    }

    public Long getKpId() 
    {
        return kpId;
    }

    public void setIsConfirmed(Integer isConfirmed) 
    {
        this.isConfirmed = isConfirmed;
    }

    public Integer getIsConfirmed() 
    {
        return isConfirmed;
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
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("resourceId", getResourceId())
            .append("kpId", getKpId())
            .append("isConfirmed", getIsConfirmed())
            .append("createTime", getCreateTime())
            .toString();
    }
}
