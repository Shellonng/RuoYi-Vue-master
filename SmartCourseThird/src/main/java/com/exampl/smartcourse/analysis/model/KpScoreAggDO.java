package com.exampl.smartcourse.analysis.model;

/**
 * 知识点得分聚合结果。
 */
public class KpScoreAggDO {

    private Long kpId;

    private String kpTitle;

    /**
     * 学生或班级在该知识点的平均得分比例（0~1）。
     */
    private Double scoreRatio;

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

    public Double getScoreRatio() {
        return scoreRatio;
    }

    public void setScoreRatio(Double scoreRatio) {
        this.scoreRatio = scoreRatio;
    }
}
