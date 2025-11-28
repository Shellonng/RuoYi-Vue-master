package com.ruoyi.system.service.impl;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.VideoLearningBehaviorMapper;
import com.ruoyi.system.domain.VideoLearningBehavior;
import com.ruoyi.system.service.IVideoLearningBehaviorService;

/**
 * 视频学习行为Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-11-28
 */
@Service
public class VideoLearningBehaviorServiceImpl implements IVideoLearningBehaviorService 
{
    @Autowired
    private VideoLearningBehaviorMapper videoLearningBehaviorMapper;

    /**
     * 查询视频学习行为
     * 
     * @param id 视频学习行为主键
     * @return 视频学习行为
     */
    @Override
    public VideoLearningBehavior selectVideoLearningBehaviorById(Long id)
    {
        return videoLearningBehaviorMapper.selectVideoLearningBehaviorById(id);
    }

    /**
     * 查询视频学习行为列表
     * 
     * @param videoLearningBehavior 视频学习行为
     * @return 视频学习行为
     */
    @Override
    public List<VideoLearningBehavior> selectVideoLearningBehaviorList(VideoLearningBehavior videoLearningBehavior)
    {
        return videoLearningBehaviorMapper.selectVideoLearningBehaviorList(videoLearningBehavior);
    }

    /**
     * 获取今日各时间点的活跃人数统计
     * 
     * @return 时间点->活跃人数的映射
     */
    @Override
    public Map<String, Integer> getTodayActiveUserStats()
    {
        // 初始化24小时的统计数据
        Map<String, Integer> hourlyStats = new LinkedHashMap<>();
        for (int i = 0; i < 24; i++) {
            hourlyStats.put(i + ":00", 0);
        }

        // 获取今日所有学习行为记录
        List<VideoLearningBehavior> behaviors = videoLearningBehaviorMapper.selectTodayLearningBehaviors();
        
        if (behaviors == null || behaviors.isEmpty()) {
            return hourlyStats;
        }

        // 统计每个小时的观看人数
        // 使用Set来记录每个小时有哪些学生在观看（去重）
        Map<Integer, Set<Long>> hourlyStudents = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            hourlyStudents.put(i, new HashSet<>());
        }

        // 遍历所有学习行为
        for (VideoLearningBehavior behavior : behaviors) {
            if (behavior.getLastWatchAt() == null || behavior.getWatchDuration() == null) {
                continue;
            }

            Date lastWatchAt = behavior.getLastWatchAt();
            int watchDuration = behavior.getWatchDuration();
            Long studentId = behavior.getStudentId();

            // 计算观看的开始时间和结束时间
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(lastWatchAt);
            
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(lastWatchAt);
            startCal.add(Calendar.SECOND, -watchDuration);

            // 如果开始时间不是今天，则从今天0点开始
            Calendar todayStart = Calendar.getInstance();
            todayStart.set(Calendar.HOUR_OF_DAY, 0);
            todayStart.set(Calendar.MINUTE, 0);
            todayStart.set(Calendar.SECOND, 0);
            
            if (startCal.before(todayStart)) {
                startCal = todayStart;
            }

            // 标记该学生在哪些小时观看了视频
            int startHour = startCal.get(Calendar.HOUR_OF_DAY);
            int endHour = endCal.get(Calendar.HOUR_OF_DAY);

            for (int hour = startHour; hour <= endHour; hour++) {
                if (hour >= 0 && hour < 24) {
                    hourlyStudents.get(hour).add(studentId);
                }
            }
        }

        // 将统计结果转换为最终格式
        for (int i = 0; i < 24; i++) {
            hourlyStats.put(i + ":00", hourlyStudents.get(i).size());
        }

        return hourlyStats;
    }

    /**
     * 获取今日各视频的分时活跃人数统计
     * 
     * @return 视频ID -> (时间点 -> 活跃人数) 的映射
     */
    @Override
    public Map<Long, Map<String, Integer>> getTodayActiveUserStatsByVideo() {
        // 初始化每个视频的24小时统计
        Map<Long, Map<String, Integer>> videoHourlyStats = new LinkedHashMap<>();
        // 获取今日所有学习行为记录
        List<VideoLearningBehavior> behaviors = videoLearningBehaviorMapper.selectTodayLearningBehaviors();
        if (behaviors == null || behaviors.isEmpty()) {
            return videoHourlyStats;
        }
        // 统计每个视频每小时的观看人数（去重）
        Map<Long, Map<Integer, Set<Long>>> videoHourStudentSet = new HashMap<>();
        for (VideoLearningBehavior behavior : behaviors) {
            if (behavior.getLastWatchAt() == null || behavior.getWatchDuration() == null) {
                continue;
            }
            Long videoId = behavior.getVideoId();
            Long studentId = behavior.getStudentId();
            Date lastWatchAt = behavior.getLastWatchAt();
            int watchDuration = behavior.getWatchDuration();
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(lastWatchAt);
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(lastWatchAt);
            startCal.add(Calendar.SECOND, -watchDuration);
            Calendar todayStart = Calendar.getInstance();
            todayStart.set(Calendar.HOUR_OF_DAY, 0);
            todayStart.set(Calendar.MINUTE, 0);
            todayStart.set(Calendar.SECOND, 0);
            if (startCal.before(todayStart)) {
                startCal = todayStart;
            }
            int startHour = startCal.get(Calendar.HOUR_OF_DAY);
            int endHour = endCal.get(Calendar.HOUR_OF_DAY);
            // 初始化该视频的小时统计
            videoHourStudentSet.putIfAbsent(videoId, new HashMap<>());
            for (int hour = startHour; hour <= endHour; hour++) {
                if (hour >= 0 && hour < 24) {
                    videoHourStudentSet.get(videoId).putIfAbsent(hour, new HashSet<>());
                    videoHourStudentSet.get(videoId).get(hour).add(studentId);
                }
            }
        }
        // 转换为前端需要的格式
        for (Map.Entry<Long, Map<Integer, Set<Long>>> entry : videoHourStudentSet.entrySet()) {
            Long videoId = entry.getKey();
            Map<String, Integer> hourlyStats = new LinkedHashMap<>();
            for (int i = 0; i < 24; i++) {
                Set<Long> students = entry.getValue().getOrDefault(i, Collections.emptySet());
                hourlyStats.put(i + ":00", students.size());
            }
            videoHourlyStats.put(videoId, hourlyStats);
        }
        return videoHourlyStats;
    }
    
    /**
     * 获取视频ID到小节标题的映射
     */
    @Override
    public Map<Long, String> getVideoIdToSectionTitleMap() {
        Map<Long, String> videoTitleMap = new HashMap<>();
        List<VideoLearningBehavior> behaviors = videoLearningBehaviorMapper.selectTodayLearningBehaviors();
        if (behaviors == null || behaviors.isEmpty()) {
            return videoTitleMap;
        }
        // 获取所有涉及的视频ID
        Set<Long> videoIds = new HashSet<>();
        for (VideoLearningBehavior behavior : behaviors) {
            videoIds.add(behavior.getVideoId());
        }
        // 查询每个视频对应的小节标题
        for (Long videoId : videoIds) {
            String title = videoLearningBehaviorMapper.selectSectionTitleByVideoId(videoId);
            if (title != null && !title.isEmpty()) {
                videoTitleMap.put(videoId, title);
            } else {
                videoTitleMap.put(videoId, "视频" + videoId);
            }
        }
        return videoTitleMap;
    }
}
