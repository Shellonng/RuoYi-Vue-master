package com.exampl.smartcourse.mastery.dto;

/**
 * 班级层面知识点统计。
 */
public class KPMasteryAggDTO {

    private Long kpId;

    private String kpTitle;

    private Double averageScore;

    private Double standardDeviation;

    private Integer lowScoreCount;

    private Integer studentCount;

    /**
     * 班级内排序，帮助快速定位掌握情况。
     */
    private Integer rank;

    /**
     * 低于阈值的学生占比（0~100）。
     */
    private Double lowScoreRate;

    /**
     * 针对该知识点的诊断建议。
     */
    private String diagnosis;

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

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(Double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public Integer getLowScoreCount() {
        return lowScoreCount;
    }

    public void setLowScoreCount(Integer lowScoreCount) {
        this.lowScoreCount = lowScoreCount;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Double getLowScoreRate() {
        return lowScoreRate;
    }

    public void setLowScoreRate(Double lowScoreRate) {
        this.lowScoreRate = lowScoreRate;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
}
