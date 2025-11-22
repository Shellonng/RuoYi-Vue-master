package com.ruoyi.system.domain;

import java.util.Date;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 知识点关系表 kp_relation
 *
 * @author ruoyi
 */
public class KpRelation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 关系ID */
    private Long id;

    /** 源知识点ID */
    @NotNull(message = "源知识点ID不能为空")
    private Long fromKpId;

    /** 目标知识点ID */
    @NotNull(message = "目标知识点ID不能为空")
    private Long toKpId;

    /** 关系类型：prerequisite_of-前置关系, similar_to-相似关系, extension_of-扩展关系, derived_from-派生关系, counterexample_of-反例关系 */
    @NotNull(message = "关系类型不能为空")
    private String relationType;

    /** 是否AI生成 */
    private Integer aiGenerated;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // 用于前端展示的扩展字段
    private String fromKpTitle;
    private String toKpTitle;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFromKpId() {
        return fromKpId;
    }

    public void setFromKpId(Long fromKpId) {
        this.fromKpId = fromKpId;
    }

    public Long getToKpId() {
        return toKpId;
    }

    public void setToKpId(Long toKpId) {
        this.toKpId = toKpId;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public Integer getAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(Integer aiGenerated) {
        this.aiGenerated = aiGenerated;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getFromKpTitle() {
        return fromKpTitle;
    }

    public void setFromKpTitle(String fromKpTitle) {
        this.fromKpTitle = fromKpTitle;
    }

    public String getToKpTitle() {
        return toKpTitle;
    }

    public void setToKpTitle(String toKpTitle) {
        this.toKpTitle = toKpTitle;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("fromKpId", getFromKpId())
                .append("toKpId", getToKpId())
                .append("relationType", getRelationType())
                .append("aiGenerated", getAiGenerated())
                .append("createTime", getCreateTime())
                .toString();
    }
}
