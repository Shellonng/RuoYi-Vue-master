package com.exampl.smartcourse.dto.response;

public class RiskItem {
    private Long studentId;
    private Long courseId;
    private Long kpId;
    private String kpTitle;
    private Double masteryScore;
    private String masteryStatus;
    private Double avgCompletionRate;
    private Double activityScore;
    private String riskType;
    private String riskLevel;
    private String suggestion;

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

    public Long getKpId() {
        return kpId;
    }

    public void setKpId(Long kpId) {
        this.kpId = kpId;
    }

    public String getKpTitle() {
        return kpTitle;
    }

    public void setKpTitle(String kpTitle) {
        this.kpTitle = kpTitle;
    }

    public Double getMasteryScore() {
        return masteryScore;
    }

    public void setMasteryScore(Double masteryScore) {
        this.masteryScore = masteryScore;
    }

    public String getMasteryStatus() {
        return masteryStatus;
    }

    public void setMasteryStatus(String masteryStatus) {
        this.masteryStatus = masteryStatus;
    }

    public Double getAvgCompletionRate() {
        return avgCompletionRate;
    }

    public void setAvgCompletionRate(Double avgCompletionRate) {
        this.avgCompletionRate = avgCompletionRate;
    }

    public Double getActivityScore() {
        return activityScore;
    }

    public void setActivityScore(Double activityScore) {
        this.activityScore = activityScore;
    }

    public String getRiskType() {
        return riskType;
    }

    public void setRiskType(String riskType) {
        this.riskType = riskType;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}

