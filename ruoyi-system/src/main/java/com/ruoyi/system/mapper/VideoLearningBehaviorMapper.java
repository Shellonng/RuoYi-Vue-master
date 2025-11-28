package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.VideoLearningBehavior;

/**
 * 视频学习行为Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-28
 */
public interface VideoLearningBehaviorMapper 
{
    /**
     * 查询视频学习行为
     * 
     * @param id 视频学习行为主键
     * @return 视频学习行为
     */
    public VideoLearningBehavior selectVideoLearningBehaviorById(Long id);

    /**
     * 查询视频学习行为列表
     * 
     * @param videoLearningBehavior 视频学习行为
     * @return 视频学习行为集合
     */
    public List<VideoLearningBehavior> selectVideoLearningBehaviorList(VideoLearningBehavior videoLearningBehavior);

    /**
     * 查询今日所有学习行为记录（用于统计活跃人数）
     * 
     * @return 视频学习行为集合
     */
    public List<VideoLearningBehavior> selectTodayLearningBehaviors();
    
    /**
     * 根据视频ID查询对应的小节标题
     * 
     * @param videoId 视频ID
     * @return 小节标题
     */
    public String selectSectionTitleByVideoId(Long videoId);
}
