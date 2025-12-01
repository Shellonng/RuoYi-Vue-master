package com.exampl.smartcourse.analysis.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于知识点的成绩分析响应。
 */
public class PerformanceAnalysisResponse {

    private List<KnowledgeScoreItem> knowledgeScores = new ArrayList<>();

    private List<KnowledgeScoreItem> weakKnowledge = new ArrayList<>();

    private List<ScoreTrendPoint> scoreTrend = new ArrayList<>();

    /**
     * 学生整体平均得分（0~1）。
     */
    private Double overallAverageScore;

    /**
     * 学业建议。
     */
    private List<String> suggestions = new ArrayList<>();

    public List<KnowledgeScoreItem> getKnowledgeScores() {
        return knowledgeScores;
    }

    public void setKnowledgeScores(List<KnowledgeScoreItem> knowledgeScores) {
        this.knowledgeScores = knowledgeScores;
    }

    public List<KnowledgeScoreItem> getWeakKnowledge() {
        return weakKnowledge;
    }

    public void setWeakKnowledge(List<KnowledgeScoreItem> weakKnowledge) {
        this.weakKnowledge = weakKnowledge;
    }

    public List<ScoreTrendPoint> getScoreTrend() {
        return scoreTrend;
    }

    public void setScoreTrend(List<ScoreTrendPoint> scoreTrend) {
        this.scoreTrend = scoreTrend;
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

    public static class KnowledgeScoreItem {
        private Long kpId;
        private String kpTitle;
        /**
         * 作业/考试得分折算（0~1）。
         */
        private Double scoreRatio;
        /**
         * 知识点掌握度（0~1），来自 student_kp_mastery。
         */
        private Double masteryScore;
        private String trend;

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

        public Double getMasteryScore() {
            return masteryScore;
        }

        public void setMasteryScore(Double masteryScore) {
            this.masteryScore = masteryScore;
        }

        public String getTrend() {
            return trend;
        }

        public void setTrend(String trend) {
            this.trend = trend;
        }

    }

    public static class ScoreTrendPoint {
        /**
         * 时间戳（毫秒）。
         */
        private Long date;
        /**
         * 本次作业/考试得分百分比（0~100）。
         */
        private Double scoreRatio;

        public Long getDate() {
            return date;
        }

        public void setDate(Long date) {
            this.date = date;
        }

        public Double getScoreRatio() {
            return scoreRatio;
        }

        public void setScoreRatio(Double scoreRatio) {
            this.scoreRatio = scoreRatio;
        }
    }
}
