package com.ruoyi.system.domain;

import java.util.Date;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 小节与知识点关联表 section_kp
 *
 * @author ruoyi
 */
public class SectionKp extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 关联ID */
    private Long id;

    /** 小节ID */
    @NotNull(message = "小节ID不能为空")
    private Long sectionId;

    /** 知识点ID */
    @NotNull(message = "知识点ID不能为空")
    private Long kpId;

    /** 在小节中的顺序 */
    private Integer sequence;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 知识点对象（用于关联查询） */
    private KnowledgePoint knowledgePoint;

    /** 小节标题（用于关联查询） */
    private String sectionTitle;

    /** 小节排序（用于关联查询） */
    private Integer sectionOrder;

    /** 章节ID（用于关联查询） */
    private Long chapterId;

    /** 章节标题（用于关联查询） */
    private String chapterTitle;

    /** 章节排序（用于关联查询） */
    private Integer chapterOrder;

    /** 课程ID（用于关联查询） */
    private Long courseId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
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

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public Integer getSectionOrder() {
        return sectionOrder;
    }

    public void setSectionOrder(Integer sectionOrder) {
        this.sectionOrder = sectionOrder;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public Integer getChapterOrder() {
        return chapterOrder;
    }

    public void setChapterOrder(Integer chapterOrder) {
        this.chapterOrder = chapterOrder;
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
            .append("sectionId", getSectionId())
            .append("kpId", getKpId())
            .append("sequence", getSequence())
            .append("createTime", getCreateTime())
            .append("knowledgePoint", getKnowledgePoint())
            .append("sectionTitle", getSectionTitle())
            .append("sectionOrder", getSectionOrder())
            .append("chapterId", getChapterId())
            .append("chapterTitle", getChapterTitle())
            .append("chapterOrder", getChapterOrder())
            .append("courseId", getCourseId())
            .toString();
    }
}
