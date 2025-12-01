package com.exampl.smartcourse.mastery.dto;

import java.util.List;

/**
 * 班级知识点掌握度响应。
 */
public class ClassMasteryResponse {

    private List<KPMasteryAggDTO> kpStats;

    /**
     * 按知识点排序的薄弱知识点列表。
     */
    private List<KPMasteryAggDTO> weakKps;

    /**
     * 班级整体平均掌握度。
     */
    private Double overallAverageScore;

    /**
     * 面向教学的建议文案。
     */
    private List<String> suggestions;

    public List<KPMasteryAggDTO> getKpStats() {
        return kpStats;
    }

    public void setKpStats(List<KPMasteryAggDTO> kpStats) {
        this.kpStats = kpStats;
    }

    public List<KPMasteryAggDTO> getWeakKps() {
        return weakKps;
    }

    public void setWeakKps(List<KPMasteryAggDTO> weakKps) {
        this.weakKps = weakKps;
    }

    public Double getOverallAverageScore() {
        return overallAverageScore;
    }

    public void setOverallAverageScore(Double overallAverageScore) {
        this.overallAverageScore = overallAverageScore;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
