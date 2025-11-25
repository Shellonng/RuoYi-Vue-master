package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 选课申请对象 course_enrollment_request
 */
public class CourseEnrollmentRequest extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 申请ID */
    private Long id;

    /** 学生user.id */
    @Excel(name = "学生ID")
    private Long studentUserId;

    /** 学生姓名 */
    @Excel(name = "学生姓名")
    private String studentName;

    /** 课程ID */
    @Excel(name = "课程ID")
    private Long courseId;

    /** 课程名称 */
    @Excel(name = "课程名称")
    private String courseName;

    /** 申请状态：0=待审核 1=已通过 2=已拒绝 */
    @Excel(name = "状态", readConverterExp = "0=待审核,1=已通过,2=已拒绝")
    private Integer status;

    /** 学生申请理由 */
    @Excel(name = "申请理由")
    private String reason;

    /** 教师审核意见 */
    @Excel(name = "审核意见")
    private String reviewComment;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(Long studentUserId) {
        this.studentUserId = studentUserId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Date getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime) {
        this.reviewTime = reviewTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("studentUserId", getStudentUserId())
                .append("studentName", getStudentName())
                .append("courseId", getCourseId())
                .append("courseName", getCourseName())
                .append("status", getStatus())
                .append("reason", getReason())
                .append("reviewComment", getReviewComment())
                .append("submitTime", getSubmitTime())
                .append("reviewTime", getReviewTime())
                .toString();
    }
}
