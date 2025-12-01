package com.exampl.smartcourse.mastery.dto;

import java.time.LocalDateTime;

/**
 * 学生知识点掌握度 DTO。
 */
public class MasteryDTO {

    private Long kpId;

    private String kpTitle;

    /**
     * 0~1 之间的掌握度分数。
     */
    private Double masteryScore;

    private String masteryStatus;

    private String trend;

    private LocalDateTime updateTime;

    /**
     * 按百分比表示的掌握率，便于前端直接渲染。
     */
    private Double accuracyRate;

    /**
     * 对掌握状态的描述，帮助老师理解学生掌握水平。
     */
    private String masteryDescription;

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

    public Double getAccuracyRate() {
        return accuracyRate;
    }

    public void setAccuracyRate(Double accuracyRate) {
        this.accuracyRate = accuracyRate;
    }

    public String getMasteryDescription() {
        return masteryDescription;
    }

    public void setMasteryDescription(String masteryDescription) {
        this.masteryDescription = masteryDescription;
    }
}
