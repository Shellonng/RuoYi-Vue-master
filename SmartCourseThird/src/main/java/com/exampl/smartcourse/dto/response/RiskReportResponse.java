package com.exampl.smartcourse.dto.response;

import java.util.List;
import java.util.Map;

public class RiskReportResponse {
    private Map<String, Long> riskTypeCount;
    private List<KpRiskAggregate> kpRiskList;
    private List<RiskItem> highRiskStudents;
    private Double overallLowMasteryRatio;
    private String summary;

    public Map<String, Long> getRiskTypeCount() {
        return riskTypeCount;
    }

    public void setRiskTypeCount(Map<String, Long> riskTypeCount) {
        this.riskTypeCount = riskTypeCount;
    }

    public List<KpRiskAggregate> getKpRiskList() {
        return kpRiskList;
    }

    public void setKpRiskList(List<KpRiskAggregate> kpRiskList) {
        this.kpRiskList = kpRiskList;
    }

    public List<RiskItem> getHighRiskStudents() {
        return highRiskStudents;
    }

    public void setHighRiskStudents(List<RiskItem> highRiskStudents) {
        this.highRiskStudents = highRiskStudents;
    }

    public Double getOverallLowMasteryRatio() {
        return overallLowMasteryRatio;
    }

    public void setOverallLowMasteryRatio(Double overallLowMasteryRatio) {
        this.overallLowMasteryRatio = overallLowMasteryRatio;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}

