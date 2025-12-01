package com.exampl.smartcourse.dto.response;

import java.util.List;
import java.util.Map;

public class StudentRiskResponse {
    private int totalRisks;
    private Map<String, Long> riskTypeCount;
    private Map<String, Long> riskLevelCount;
    private List<RiskItem> risks;

    public int getTotalRisks() {
        return totalRisks;
    }

    public void setTotalRisks(int totalRisks) {
        this.totalRisks = totalRisks;
    }

    public Map<String, Long> getRiskTypeCount() {
        return riskTypeCount;
    }

    public void setRiskTypeCount(Map<String, Long> riskTypeCount) {
        this.riskTypeCount = riskTypeCount;
    }

    public Map<String, Long> getRiskLevelCount() {
        return riskLevelCount;
    }

    public void setRiskLevelCount(Map<String, Long> riskLevelCount) {
        this.riskLevelCount = riskLevelCount;
    }

    public List<RiskItem> getRisks() {
        return risks;
    }

    public void setRisks(List<RiskItem> risks) {
        this.risks = risks;
    }
}

