package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.VideoLearningBehavior;

/**
 * 视频学习行为Service接口
 * 
 * @author ruoyi
 * @date 2025-11-28
 */
public interface IVideoLearningBehaviorService 
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
     * 获取今日各时间点的活跃人数统计
     * 
     * @return 时间点->活跃人数的映射
     */
    public Map<String, Integer> getTodayActiveUserStats();

    /**
     * 获取今日各视频分时活跃人数统计
     * @return Map<视频ID, Map<小时, 人数>>
     */
    public Map<Long, Map<String, Integer>> getTodayActiveUserStatsByVideo();
    
    /**
     * 获取视频ID到小节标题的映射
     * @return Map<视频ID, 小节标题>
     */
    public Map<Long, String> getVideoIdToSectionTitleMap();
}
