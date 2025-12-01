package com.exampl.smartcourse.dto.response;

public class KpRiskAggregate {
    private Long kpId;
    private String kpTitle;
    private Double averageMastery;
    private Double masteryStdDev;
    private Long lowMasteryCount;

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

    public Double getAverageMastery() {
        return averageMastery;
    }

    public void setAverageMastery(Double averageMastery) {
        this.averageMastery = averageMastery;
    }

    public Double getMasteryStdDev() {
        return masteryStdDev;
    }

    public void setMasteryStdDev(Double masteryStdDev) {
        this.masteryStdDev = masteryStdDev;
    }

    public Long getLowMasteryCount() {
        return lowMasteryCount;
    }

    public void setLowMasteryCount(Long lowMasteryCount) {
        this.lowMasteryCount = lowMasteryCount;
    }
}

