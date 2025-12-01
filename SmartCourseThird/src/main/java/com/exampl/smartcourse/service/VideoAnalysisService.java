package com.exampl.smartcourse.service;

import com.exampl.smartcourse.dto.response.BehaviorPerformanceCorrelationResponse;
import com.exampl.smartcourse.dto.response.VideoCompletionStatsResponse;
import com.exampl.smartcourse.dto.response.VideoHeatmapResponse;
import com.exampl.smartcourse.entity.Video;
import java.util.List;

public interface VideoAnalysisService {

    VideoHeatmapResponse buildVideoHeatmap(Long videoId, Long studentId);

    VideoCompletionStatsResponse buildCompletionStats(Long courseId, Long videoId, Long studentId);

    BehaviorPerformanceCorrelationResponse analyzeBehaviorWithPerformance(Long courseId, Long videoId);

    List<Video> getVideos(Long courseId);

    com.exampl.smartcourse.llmclient.aiGrading.SiliconFlowClient.VideoAiResult analyzeBehaviorAi(Long courseId, Long videoId, String model);
}
