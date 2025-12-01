package com.exampl.smartcourse.dto;

public class StudentBehaviorAggregate {
    private Long studentId;
    private Long courseId;
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

