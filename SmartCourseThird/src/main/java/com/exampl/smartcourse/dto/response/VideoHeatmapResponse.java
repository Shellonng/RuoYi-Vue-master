package com.exampl.smartcourse.dto.response;

import java.util.List;

public class VideoHeatmapResponse {

    private Long videoId;
    private Long courseId;
    private Long studentId;
    private int uniqueStudents;
    private long totalWatchDuration;
    private double averageCompletionRate;
    private List<HeatmapBucket> heatmapBuckets;

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public int getUniqueStudents() {
        return uniqueStudents;
    }

    public void setUniqueStudents(int uniqueStudents) {
        this.uniqueStudents = uniqueStudents;
    }

    public long getTotalWatchDuration() {
        return totalWatchDuration;
    }

    public void setTotalWatchDuration(long totalWatchDuration) {
        this.totalWatchDuration = totalWatchDuration;
    }

    public double getAverageCompletionRate() {
        return averageCompletionRate;
    }

    public void setAverageCompletionRate(double averageCompletionRate) {
        this.averageCompletionRate = averageCompletionRate;
    }

    public List<HeatmapBucket> getHeatmapBuckets() {
        return heatmapBuckets;
    }

    public void setHeatmapBuckets(List<HeatmapBucket> heatmapBuckets) {
        this.heatmapBuckets = heatmapBuckets;
    }

    public static class HeatmapBucket {
        private String range;
        private long count;

        public HeatmapBucket() {
        }

        public HeatmapBucket(String range, long count) {
            this.range = range;
            this.count = count;
        }

        public String getRange() {
            return range;
        }

        public void setRange(String range) {
            this.range = range;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}

