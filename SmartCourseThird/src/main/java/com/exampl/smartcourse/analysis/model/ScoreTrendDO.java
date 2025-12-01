package com.exampl.smartcourse.analysis.model;

/**
 * 作业/考试得分趋势点。
 */
public class ScoreTrendDO {

    /**
     * 时间戳（毫秒）。
     */
    private Long timestamp;

    private Double scoreRatio;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getScoreRatio() {
        return scoreRatio;
    }

    public void setScoreRatio(Double scoreRatio) {
        this.scoreRatio = scoreRatio;
    }
}
