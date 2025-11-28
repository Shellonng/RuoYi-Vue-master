package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 视频学习行为对象 video_learning_behavior
 * 
 * @author ruoyi
 * @date 2025-11-28
 */
public class VideoLearningBehavior extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 行为ID */
    private Long id;

    /** 学生ID */
    @Excel(name = "学生ID")
    private Long studentId;

    /** 视频ID */
    @Excel(name = "视频ID")
    private Long videoId;

    /** 观看时长（秒） */
    @Excel(name = "观看时长", readConverterExp = "秒=")
    private Integer watchDuration;

    /** 视频总时长（秒） */
    @Excel(name = "视频总时长", readConverterExp = "秒=")
    private Integer videoDuration;

    /** 完成率（%） */
    @Excel(name = "完成率", readConverterExp = "%=")
    private BigDecimal completionRate;

    /** 观看次数 */
    @Excel(name = "观看次数")
    private Integer watchCount;

    /** 热力图数据 */
    private String heatmapData;

    /** 是否看完：1-是 0-否 */
    @Excel(name = "是否看完：1-是 0-否")
    private Integer isCompleted;

    /** 快进次数 */
    @Excel(name = "快进次数")
    private Integer fastForwardCount;

    /** 暂停次数 */
    @Excel(name = "暂停次数")
    private Integer pauseCount;

    /** 播放倍速 */
    @Excel(name = "播放倍速")
    private BigDecimal playbackSpeed;

    /** 首次观看时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "首次观看时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date firstWatchAt;

    /** 最近观看时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最近观看时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastWatchAt;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setStudentId(Long studentId) 
    {
        this.studentId = studentId;
    }

    public Long getStudentId() 
    {
        return studentId;
    }

    public void setVideoId(Long videoId) 
    {
        this.videoId = videoId;
    }

    public Long getVideoId() 
    {
        return videoId;
    }

    public void setWatchDuration(Integer watchDuration) 
    {
        this.watchDuration = watchDuration;
    }

    public Integer getWatchDuration() 
    {
        return watchDuration;
    }

    public void setVideoDuration(Integer videoDuration) 
    {
        this.videoDuration = videoDuration;
    }

    public Integer getVideoDuration() 
    {
        return videoDuration;
    }

    public void setCompletionRate(BigDecimal completionRate) 
    {
        this.completionRate = completionRate;
    }

    public BigDecimal getCompletionRate() 
    {
        return completionRate;
    }

    public void setWatchCount(Integer watchCount) 
    {
        this.watchCount = watchCount;
    }

    public Integer getWatchCount() 
    {
        return watchCount;
    }

    public void setHeatmapData(String heatmapData) 
    {
        this.heatmapData = heatmapData;
    }

    public String getHeatmapData() 
    {
        return heatmapData;
    }

    public void setIsCompleted(Integer isCompleted) 
    {
        this.isCompleted = isCompleted;
    }

    public Integer getIsCompleted() 
    {
        return isCompleted;
    }

    public void setFastForwardCount(Integer fastForwardCount) 
    {
        this.fastForwardCount = fastForwardCount;
    }

    public Integer getFastForwardCount() 
    {
        return fastForwardCount;
    }

    public void setPauseCount(Integer pauseCount) 
    {
        this.pauseCount = pauseCount;
    }

    public Integer getPauseCount() 
    {
        return pauseCount;
    }

    public void setPlaybackSpeed(BigDecimal playbackSpeed) 
    {
        this.playbackSpeed = playbackSpeed;
    }

    public BigDecimal getPlaybackSpeed() 
    {
        return playbackSpeed;
    }

    public void setFirstWatchAt(Date firstWatchAt) 
    {
        this.firstWatchAt = firstWatchAt;
    }

    public Date getFirstWatchAt() 
    {
        return firstWatchAt;
    }

    public void setLastWatchAt(Date lastWatchAt) 
    {
        this.lastWatchAt = lastWatchAt;
    }

    public Date getLastWatchAt() 
    {
        return lastWatchAt;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    public void setUpdatedAt(Date updatedAt) 
    {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() 
    {
        return updatedAt;
    }
}
