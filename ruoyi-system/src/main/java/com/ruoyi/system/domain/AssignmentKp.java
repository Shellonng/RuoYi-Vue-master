package com.ruoyi.system.domain;

import java.util.Date;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 作业与知识点关联表 assignment_kp
 *
 * @author ruoyi
 */
public class AssignmentKp extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 关联ID */
    private Long id;

    /** 作业ID */
    @NotNull(message = "作业ID不能为空")
    private Long assignmentId;

    /** 知识点ID */
    @NotNull(message = "知识点ID不能为空")
    private Long kpId;

    /** 顺序 */
    private Integer sequence;

    /** 是否必须（0否，1是） */
    private Integer isRequired;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 知识点对象（用于关联查询） */
    private KnowledgePoint knowledgePoint;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getKpId() {
        return kpId;
    }

    public void setKpId(Long kpId) {
        this.kpId = kpId;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public Integer getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Integer isRequired) {
        this.isRequired = isRequired;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public KnowledgePoint getKnowledgePoint() {
        return knowledgePoint;
    }

    public void setKnowledgePoint(KnowledgePoint knowledgePoint) {
        this.knowledgePoint = knowledgePoint;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("assignmentId", getAssignmentId())
            .append("kpId", getKpId())
            .append("sequence", getSequence())
            .append("isRequired", getIsRequired())
            .append("createTime", getCreateTime())
            .append("knowledgePoint", getKnowledgePoint())
            .toString();
    }
}
