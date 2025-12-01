package com.exampl.smartcourse.controller;

import com.exampl.smartcourse.common.ErrorCode;
import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.dto.response.BehaviorPerformanceCorrelationResponse;
import com.exampl.smartcourse.dto.response.VideoCompletionStatsResponse;
import com.exampl.smartcourse.dto.response.VideoHeatmapResponse;
import com.exampl.smartcourse.entity.Video;
import com.exampl.smartcourse.service.VideoAnalysisService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis/video")
public class VideoAnalysisController {

    private final VideoAnalysisService videoAnalysisService;

    public VideoAnalysisController(VideoAnalysisService videoAnalysisService) {
        this.videoAnalysisService = videoAnalysisService;
    }

    @GetMapping("/heatmap")
    public Result<VideoHeatmapResponse> getHeatmap(@RequestParam Long videoId,
            @RequestParam(required = false) Long studentId) {
        try {
            return Result.success(videoAnalysisService.buildVideoHeatmap(videoId, studentId));
        } catch (IllegalArgumentException ex) {
            return Result.failure(ErrorCode.ERROR, ex.getMessage());
        }
    }

    @GetMapping("/completion")
    public Result<VideoCompletionStatsResponse> getCompletionStats(@RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long videoId,
            @RequestParam(required = false) Long studentId) {
        return Result.success(videoAnalysisService.buildCompletionStats(courseId, videoId, studentId));
    }

    @GetMapping("/behavior-correlation")
    public Result<BehaviorPerformanceCorrelationResponse> analyzeBehaviorCorrelation(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long videoId) {
        return Result.success(videoAnalysisService.analyzeBehaviorWithPerformance(courseId, videoId));
    }

    @GetMapping("/behavior-ai")
    public Result<com.exampl.smartcourse.llmclient.aiGrading.SiliconFlowClient.VideoAiResult> analyzeBehaviorAi(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long videoId,
            @RequestParam(required = false) String model) {
        return Result.success(videoAnalysisService.analyzeBehaviorAi(courseId, videoId, model));
    }

    @GetMapping("/list")
    public Result<List<Video>> listVideos(@RequestParam Long courseId) {
        return Result.success(videoAnalysisService.getVideos(courseId));
    }
}
