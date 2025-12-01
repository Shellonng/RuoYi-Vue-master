package com.exampl.smartcourse.dto.response;

import java.util.List;

public class BehaviorPerformanceCorrelationResponse {

    private Long courseId;
    private int sampleSize;
    private double completionRateCorrelation;
    private double durationCorrelation;
    private double watchCountCorrelation;
    private List<StudentBehaviorPerformance> samples;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }

    public double getCompletionRateCorrelation() {
        return completionRateCorrelation;
    }

    public void setCompletionRateCorrelation(double completionRateCorrelation) {
        this.completionRateCorrelation = completionRateCorrelation;
    }

    public double getDurationCorrelation() {
        return durationCorrelation;
    }

    public void setDurationCorrelation(double durationCorrelation) {
        this.durationCorrelation = durationCorrelation;
    }

    public double getWatchCountCorrelation() {
        return watchCountCorrelation;
    }

    public void setWatchCountCorrelation(double watchCountCorrelation) {
        this.watchCountCorrelation = watchCountCorrelation;
    }

    public List<StudentBehaviorPerformance> getSamples() {
        return samples;
    }

    public void setSamples(List<StudentBehaviorPerformance> samples) {
        this.samples = samples;
    }

    public static class StudentBehaviorPerformance {
        private Long studentId;
        private Long courseId;
        private double avgCompletionRate;
        private long totalWatchDuration;
        private long totalWatchCount;
        private double avgScore;

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long studentId) {
            this.studentId = studentId;
        }

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public double getAvgCompletionRate() {
            return avgCompletionRate;
        }

        public void setAvgCompletionRate(double avgCompletionRate) {
            this.avgCompletionRate = avgCompletionRate;
        }

        public long getTotalWatchDuration() {
            return totalWatchDuration;
        }

        public void setTotalWatchDuration(long totalWatchDuration) {
            this.totalWatchDuration = totalWatchDuration;
        }

        public long getTotalWatchCount() {
            return totalWatchCount;
        }

        public void setTotalWatchCount(long totalWatchCount) {
            this.totalWatchCount = totalWatchCount;
        }

        public double getAvgScore() {
            return avgScore;
        }

        public void setAvgScore(double avgScore) {
            this.avgScore = avgScore;
        }
    }
}

