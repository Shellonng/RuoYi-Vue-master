package com.exampl.smartcourse.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对应 video_learning_behavior 表。
 */
public class VideoLearningBehavior {

    private Long id;
    private Long studentId;
    private Long videoId;
    private Integer watchDuration;
    private Integer videoDuration;
    private BigDecimal completionRate;
    private Integer watchCount;
    private String heatmapData;
    private Integer isCompleted;
    private Integer fastForwardCount;
    private Integer pauseCount;
    private BigDecimal playbackSpeed;
    private LocalDateTime firstWatchAt;
    private LocalDateTime lastWatchAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public Integer getWatchDuration() {
        return watchDuration;
    }

    public void setWatchDuration(Integer watchDuration) {
        this.watchDuration = watchDuration;
    }

    public Integer getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(Integer videoDuration) {
        this.videoDuration = videoDuration;
    }

    public BigDecimal getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(BigDecimal completionRate) {
        this.completionRate = completionRate;
    }

    public Integer getWatchCount() {
        return watchCount;
    }

    public void setWatchCount(Integer watchCount) {
        this.watchCount = watchCount;
    }

    public String getHeatmapData() {
        return heatmapData;
    }

    public void setHeatmapData(String heatmapData) {
        this.heatmapData = heatmapData;
    }

    public Integer getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Integer isCompleted) {
        this.isCompleted = isCompleted;
    }

    public Integer getFastForwardCount() {
        return fastForwardCount;
    }

    public void setFastForwardCount(Integer fastForwardCount) {
        this.fastForwardCount = fastForwardCount;
    }

    public Integer getPauseCount() {
        return pauseCount;
    }

    public void setPauseCount(Integer pauseCount) {
        this.pauseCount = pauseCount;
    }

    public BigDecimal getPlaybackSpeed() {
        return playbackSpeed;
    }

    public void setPlaybackSpeed(BigDecimal playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
    }

    public LocalDateTime getFirstWatchAt() {
        return firstWatchAt;
    }

    public void setFirstWatchAt(LocalDateTime firstWatchAt) {
        this.firstWatchAt = firstWatchAt;
    }

    public LocalDateTime getLastWatchAt() {
        return lastWatchAt;
    }

    public void setLastWatchAt(LocalDateTime lastWatchAt) {
        this.lastWatchAt = lastWatchAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

