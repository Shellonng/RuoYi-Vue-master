package com.exampl.smartcourse.analysis.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识点错题明细响应。
 */
public class KnowledgeErrorResponse {

    private String knowledgePoint;

    private List<ErrorQuestionItem> errorList = new ArrayList<>();

    public String getKnowledgePoint() {
        return knowledgePoint;
    }

    public void setKnowledgePoint(String knowledgePoint) {
        this.knowledgePoint = knowledgePoint;
    }

    public List<ErrorQuestionItem> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<ErrorQuestionItem> errorList) {
        this.errorList = errorList;
    }

    public static class ErrorQuestionItem {

        private String questionTitle;

        private String difficulty;

        private Integer errorCount;

        private String correctRate;

        public String getQuestionTitle() {
            return questionTitle;
        }

        public void setQuestionTitle(String questionTitle) {
            this.questionTitle = questionTitle;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
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
