package com.exampl.smartcourse.mastery.model;

import java.time.LocalDateTime;

/**
 * 数据库映射：学生-知识点掌握记录。
 */
public class StudentKPMasteryDO {

    private Long kpId;

    private String kpTitle;

    private Long studentUserId;

    private Double masteryScore;

    private String masteryStatus;

    private String trend;

    private LocalDateTime updateTime;

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

    public Long getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(Long studentUserId) {
        this.studentUserId = studentUserId;
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

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
