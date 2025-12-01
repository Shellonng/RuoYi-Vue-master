package com.exampl.smartcourse.dto.response;

import java.util.Map;

public class VideoCompletionStatsResponse {

    private Long courseId;
    private Long videoId;
    private long totalRecords;
    private long completedCount;
    private double completionRate;
    private double averageCompletionRate;
    private long totalWatchDuration;
    private int uniqueStudents;
    private Map<String, Long> completionDistribution;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public double getAverageCompletionRate() {
        return averageCompletionRate;
    }

    public void setAverageCompletionRate(double averageCompletionRate) {
        this.averageCompletionRate = averageCompletionRate;
    }

    public long getTotalWatchDuration() {
        return totalWatchDuration;
    }

    public void setTotalWatchDuration(long totalWatchDuration) {
        this.totalWatchDuration = totalWatchDuration;
    }

    public int getUniqueStudents() {
        return uniqueStudents;
    }

    public void setUniqueStudents(int uniqueStudents) {
        this.uniqueStudents = uniqueStudents;
    }

    public Map<String, Long> getCompletionDistribution() {
        return completionDistribution;
    }

    public void setCompletionDistribution(Map<String, Long> completionDistribution) {
        this.completionDistribution = completionDistribution;
    }
}

