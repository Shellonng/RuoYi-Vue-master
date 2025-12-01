package com.exampl.smartcourse.analysis.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 返回给前端的学习分析数据结构。
 */
public class LearningAnalysisResponse {

    private List<KnowledgeMasteryItem> knowledgeMastery = new ArrayList<>();

    private List<ScoreTrendPoint> scoreTrend = new ArrayList<>();

    private List<SubjectErrorRate> errorRate = new ArrayList<>();

    private List<WeakKnowledgeItem> weakKnowledge = new ArrayList<>();

    public List<KnowledgeMasteryItem> getKnowledgeMastery() {
        return knowledgeMastery;
    }

    public void setKnowledgeMastery(List<KnowledgeMasteryItem> knowledgeMastery) {
        this.knowledgeMastery = knowledgeMastery;
    }

    public List<ScoreTrendPoint> getScoreTrend() {
        return scoreTrend;
    }

    public void setScoreTrend(List<ScoreTrendPoint> scoreTrend) {
        this.scoreTrend = scoreTrend;
    }

    public List<SubjectErrorRate> getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(List<SubjectErrorRate> errorRate) {
        this.errorRate = errorRate;
    }

    public List<WeakKnowledgeItem> getWeakKnowledge() {
        return weakKnowledge;
    }

    public void setWeakKnowledge(List<WeakKnowledgeItem> weakKnowledge) {
        this.weakKnowledge = weakKnowledge;
    }

    public static class KnowledgeMasteryItem {
        private String knowledgePoint;
        private String rate;

        public String getKnowledgePoint() {
            return knowledgePoint;
        }

        public void setKnowledgePoint(String knowledgePoint) {
            this.knowledgePoint = knowledgePoint;
        }

        public String getRate() {
            return rate;
        }

        public void setRate(String rate) {
            this.rate = rate;
        }
    }

    public static class ScoreTrendPoint {
        private String date;
        private Double score;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }

    public static class SubjectErrorRate {
        private String subject;
        private String rate;

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getRate() {
            return rate;
        }

        public void setRate(String rate) {
            this.rate = rate;
        }
    }

    public static class WeakKnowledgeItem {
        private String knowledgePoint;
        private Integer errorCount;
        private String correctRate;

        public String getKnowledgePoint() {
            return knowledgePoint;
        }

        public void setKnowledgePoint(String knowledgePoint) {
            this.knowledgePoint = knowledgePoint;
        }

        public Integer getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(Integer errorCount) {
            this.errorCount = errorCount;
        }

        public String getCorrectRate() {
            return correctRate;
        }

        public void setCorrectRate(String correctRate) {
            this.correctRate = correctRate;
        }
    }
}
