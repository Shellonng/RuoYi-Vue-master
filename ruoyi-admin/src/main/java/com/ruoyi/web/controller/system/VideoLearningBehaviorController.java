package com.ruoyi.web.controller.system;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.IVideoLearningBehaviorService;

/**
 * 视频学习行为Controller
 * 
 * @author ruoyi
 * @date 2025-11-28
 */
@RestController
@RequestMapping("/video/learning")
public class VideoLearningBehaviorController extends BaseController
{
    @Autowired
    private IVideoLearningBehaviorService videoLearningBehaviorService;

    /**
     * 获取今日活跃人数统计
     */
    @GetMapping("/todayActiveStats")
    public AjaxResult getTodayActiveStats()
    {
        Map<String, Integer> stats = videoLearningBehaviorService.getTodayActiveUserStats();
        return AjaxResult.success(stats);
    }

    /**
     * 获取今日各视频分时活跃人数统计
     */
    @GetMapping("/todayActiveStatsByVideo")
    public AjaxResult getTodayActiveStatsByVideo()
    {
        Map<Long, Map<String, Integer>> stats = videoLearningBehaviorService.getTodayActiveUserStatsByVideo();
        return AjaxResult.success(stats);
    }
    
    /**
     * 获取视频ID到小节标题的映射
     */
    @GetMapping("/videoTitles")
    public AjaxResult getVideoTitles()
    {
        Map<Long, String> titleMap = videoLearningBehaviorService.getVideoIdToSectionTitleMap();
        return AjaxResult.success(titleMap);
    }
}
