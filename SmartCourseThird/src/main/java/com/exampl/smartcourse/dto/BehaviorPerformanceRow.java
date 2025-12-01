package com.exampl.smartcourse.dto;

/**
 * Mapper 查询学习行为与成绩关联的中间结果。
 */
public class BehaviorPerformanceRow {
    private Long studentId;
    private Long courseId;
    private Double avgScore;
    private Integer examCount;
    private Integer homeworkCount;
    private Double avgCompletionRate;
    private Long totalWatchDuration;
    private Long totalWatchCount;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Double getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(Double avgScore) {
        this.avgScore = avgScore;
    }

    public Integer getExamCount() {
        return examCount;
    }

    public void setExamCount(Integer examCount) {
        this.examCount = examCount;
    }

    public Integer getHomeworkCount() {
        return homeworkCount;
    }

    public void setHomeworkCount(Integer homeworkCount) {
        this.homeworkCount = homeworkCount;
    }

    public Double getAvgCompletionRate() {
        return avgCompletionRate;
    }

    public void setAvgCompletionRate(Double avgCompletionRate) {
        this.avgCompletionRate = avgCompletionRate;
    }

    public Long getTotalWatchDuration() {
        return totalWatchDuration;
    }

    public void setTotalWatchDuration(Long totalWatchDuration) {
        this.totalWatchDuration = totalWatchDuration;
    }

    public Long getTotalWatchCount() {
        return totalWatchCount;
    }

    public void setTotalWatchCount(Long totalWatchCount) {
        this.totalWatchCount = totalWatchCount;
    }
}

