package com.exampl.smartcourse.service.impl;

import com.exampl.smartcourse.dto.BehaviorPerformanceRow;
import com.exampl.smartcourse.dto.response.BehaviorPerformanceCorrelationResponse;
import com.exampl.smartcourse.dto.response.VideoCompletionStatsResponse;
import com.exampl.smartcourse.dto.response.VideoHeatmapResponse;
import com.exampl.smartcourse.entity.Video;
import com.exampl.smartcourse.entity.VideoLearningBehavior;
import com.exampl.smartcourse.mapper.LearningPerformanceMapper;
import com.exampl.smartcourse.mapper.VideoLearningBehaviorMapper;
import com.exampl.smartcourse.mapper.VideoMapper;
import com.exampl.smartcourse.service.VideoAnalysisService;
import com.exampl.smartcourse.llmclient.aiGrading.SiliconFlowClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Service
public class VideoAnalysisServiceImpl implements VideoAnalysisService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final VideoLearningBehaviorMapper behaviorMapper;
    private final VideoMapper videoMapper;
    private final LearningPerformanceMapper learningPerformanceMapper;
    private final SiliconFlowClient siliconFlowClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VideoAnalysisServiceImpl(VideoLearningBehaviorMapper behaviorMapper,
            VideoMapper videoMapper,
            LearningPerformanceMapper learningPerformanceMapper,
            SiliconFlowClient siliconFlowClient) {
        this.behaviorMapper = behaviorMapper;
        this.videoMapper = videoMapper;
        this.learningPerformanceMapper = learningPerformanceMapper;
        this.siliconFlowClient = siliconFlowClient;
    }

    @Override
    public VideoHeatmapResponse buildVideoHeatmap(Long videoId, Long studentId) {
        if (videoId == null) {
            throw new IllegalArgumentException("videoId 不能为空");
        }
        Video video = videoMapper.findById(videoId);
        if (video == null) {
            throw new IllegalArgumentException("视频不存在，id=" + videoId);
        }

        List<VideoLearningBehavior> behaviors = behaviorMapper.selectByVideo(videoId, studentId);
        Map<String, LongAdder> bucketCounter = new HashMap<>();
        Set<Long> uniqueStudents = new HashSet<>();
        long totalDuration = 0;
        double completionSum = 0;
        int completionCount = 0;

        for (VideoLearningBehavior behavior : behaviors) {
            if (behavior.getStudentId() != null) {
                uniqueStudents.add(behavior.getStudentId());
            }
            if (behavior.getWatchDuration() != null) {
                totalDuration += behavior.getWatchDuration();
            }
            if (behavior.getCompletionRate() != null) {
                completionSum += behavior.getCompletionRate().doubleValue();
                completionCount++;
            }
            if (behavior.getHeatmapData() != null) {
                mergeHeatmap(bucketCounter, behavior.getHeatmapData());
            }
        }

        List<VideoHeatmapResponse.HeatmapBucket> buckets = bucketCounter.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> parseBucketStart(entry.getKey())))
                .map(entry -> new VideoHeatmapResponse.HeatmapBucket(entry.getKey(), entry.getValue().longValue()))
                .collect(Collectors.toList());

        VideoHeatmapResponse response = new VideoHeatmapResponse();
        response.setVideoId(videoId);
        response.setCourseId(video.getCourseId());
        response.setStudentId(studentId);
        response.setUniqueStudents(studentId == null ? uniqueStudents.size() : (behaviors.isEmpty() ? 0 : 1));
        response.setTotalWatchDuration(totalDuration);
        response.setAverageCompletionRate(completionCount == 0 ? 0 : round(completionSum / completionCount));
        response.setHeatmapBuckets(buckets);
        return response;
    }

    @Override
    public VideoCompletionStatsResponse buildCompletionStats(Long courseId, Long videoId, Long studentId) {
        List<VideoLearningBehavior> behaviors = behaviorMapper.selectByCourseAndVideo(courseId, videoId, studentId);
        VideoCompletionStatsResponse response = new VideoCompletionStatsResponse();
        response.setCourseId(resolveCourseId(courseId, videoId));
        response.setVideoId(videoId);

        if (CollectionUtils.isEmpty(behaviors)) {
            response.setCompletionRate(0);
            response.setAverageCompletionRate(0);
            response.setCompletedCount(0);
            response.setTotalRecords(0);
            response.setTotalWatchDuration(0);
            response.setUniqueStudents(0);
            response.setCompletionDistribution(defaultDistribution());
            return response;
        }

        long totalRecords = behaviors.size();
        Map<Long, double[]> sumCountByStudent = new HashMap<>(); // [sum, count]
        for (VideoLearningBehavior b : behaviors) {
            if (b.getStudentId() == null || b.getCompletionRate() == null) continue;
            double[] sc = sumCountByStudent.computeIfAbsent(b.getStudentId(), k -> new double[]{0,0});
            sc[0] += b.getCompletionRate().doubleValue();
            sc[1] += 1;
        }
        final double COMPLETE_THRESHOLD = 100.0;
        java.util.Set<Long> completedStudents = sumCountByStudent.entrySet().stream()
                .filter(e -> e.getValue()[1] > 0 && (e.getValue()[0] / e.getValue()[1]) >= COMPLETE_THRESHOLD)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        long completedCount = completedStudents.size();
        long totalWatchDuration = behaviors.stream()
                .map(VideoLearningBehavior::getWatchDuration)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        double avgCompletion = behaviors.stream()
                .map(VideoLearningBehavior::getCompletionRate)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0);

        Map<String, Long> distribution = buildCompletionDistribution(behaviors);
        Set<Long> uniqueStudents = behaviors.stream()
                .map(VideoLearningBehavior::getStudentId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        response.setTotalRecords(totalRecords);
        response.setCompletedCount(completedCount);
        long uniqueCount = (studentId == null ? uniqueStudents.size() : (behaviors.isEmpty() ? 0 : 1));
        response.setCompletionRate(uniqueCount == 0 ? 0 : round((double) completedCount / uniqueCount * 100));
        response.setAverageCompletionRate(round(avgCompletion));
        response.setTotalWatchDuration(totalWatchDuration);
        response.setUniqueStudents((int) uniqueCount);
        response.setCompletionDistribution(distribution);
        return response;
    }

    @Override
    public BehaviorPerformanceCorrelationResponse analyzeBehaviorWithPerformance(Long courseId, Long videoId) {
        List<BehaviorPerformanceRow> rows = learningPerformanceMapper.loadBehaviorPerformance(courseId, videoId);
        BehaviorPerformanceCorrelationResponse response = new BehaviorPerformanceCorrelationResponse();
        response.setCourseId(courseId);
        response.setSampleSize(rows.size());
        if (rows.isEmpty()) {
            response.setCompletionRateCorrelation(0);
            response.setDurationCorrelation(0);
            response.setWatchCountCorrelation(0);
            response.setSamples(List.of());
            return response;
        }

        List<Double> completionRates = new ArrayList<>();
        List<Double> durations = new ArrayList<>();
        List<Double> watchCounts = new ArrayList<>();
        List<Double> scoresForCompletion = new ArrayList<>();
        List<Double> scoresForDuration = new ArrayList<>();
        List<Double> scoresForWatchCount = new ArrayList<>();

        for (BehaviorPerformanceRow row : rows) {
            if (row.getAvgScore() == null) {
                continue;
            }
            double score = row.getAvgScore();
            if (row.getAvgCompletionRate() != null) {
                completionRates.add(row.getAvgCompletionRate());
                scoresForCompletion.add(score);
            }
            if (row.getTotalWatchDuration() != null) {
                durations.add(row.getTotalWatchDuration().doubleValue());
                scoresForDuration.add(score);
            }
            if (row.getTotalWatchCount() != null) {
                watchCounts.add(row.getTotalWatchCount().doubleValue());
                scoresForWatchCount.add(score);
            }
        }

        response.setCompletionRateCorrelation(computeCorrelation(completionRates, scoresForCompletion));
        response.setDurationCorrelation(computeCorrelation(durations, scoresForDuration));
        response.setWatchCountCorrelation(computeCorrelation(watchCounts, scoresForWatchCount));
        response.setSamples(rows.stream()
                .limit(50)
                .map(this::toSample)
                .collect(Collectors.toList()));
        return response;
    }

    @Override
    public SiliconFlowClient.VideoAiResult analyzeBehaviorAi(Long courseId, Long videoId, String model) {
        BehaviorPerformanceCorrelationResponse data = analyzeBehaviorWithPerformance(courseId, videoId);
        String normalized = normalizeModel(model);
        try {
            return siliconFlowClient.analyzeVideoBehavior(data, normalized);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String normalizeModel(String m) {
        if (m != null && !m.isBlank()) {
            return m;
        }
        java.util.List<String> models = siliconFlowClient.listModels();
        if (models != null && !models.isEmpty()) {
            return models.contains("deepseek-ai/DeepSeek-V3") ? "deepseek-ai/DeepSeek-V3" : models.get(0);
        }
        return "deepseek-ai/DeepSeek-V3";
    }

    @Override
    public List<Video> getVideos(Long courseId) {
        if (courseId == null) {
            return List.of();
        }
        return videoMapper.findByCourseId(courseId);
    }

    private Map<String, Long> defaultDistribution() {
        Map<String, Long> map = new HashMap<>();
        map.put("0-50", 0L);
        map.put("50-80", 0L);
        map.put("80-100", 0L);
        return map;
    }

    private Map<String, Long> buildCompletionDistribution(List<VideoLearningBehavior> behaviors) {
        Map<String, Long> distribution = defaultDistribution();
        for (VideoLearningBehavior behavior : behaviors) {
            if (behavior.getCompletionRate() == null) {
                continue;
            }
            double rate = behavior.getCompletionRate().doubleValue();
            if (rate < 50) {
                distribution.compute("0-50", (k, v) -> v + 1);
            } else if (rate < 80) {
                distribution.compute("50-80", (k, v) -> v + 1);
            } else {
                distribution.compute("80-100", (k, v) -> v + 1);
            }
        }
        return distribution;
    }

    private void mergeHeatmap(Map<String, LongAdder> bucketCounter, String heatmapJson) {
        try {
            Map<String, Object> map = objectMapper.readValue(heatmapJson, MAP_TYPE);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                long value = convertToLong(entry.getValue());
                bucketCounter.computeIfAbsent(entry.getKey(), key -> new LongAdder()).add(value);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("解析热力图数据失败", e);
        }
    }

    private long convertToLong(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private int parseBucketStart(String bucket) {
        try {
            String[] parts = bucket.split("-");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private BehaviorPerformanceCorrelationResponse.StudentBehaviorPerformance toSample(BehaviorPerformanceRow row) {
        BehaviorPerformanceCorrelationResponse.StudentBehaviorPerformance sample = new BehaviorPerformanceCorrelationResponse.StudentBehaviorPerformance();
        sample.setStudentId(row.getStudentId());
        sample.setCourseId(row.getCourseId());
        sample.setAvgCompletionRate(row.getAvgCompletionRate() == null ? 0 : round(row.getAvgCompletionRate()));
        sample.setTotalWatchDuration(row.getTotalWatchDuration() == null ? 0 : row.getTotalWatchDuration());
        sample.setTotalWatchCount(row.getTotalWatchCount() == null ? 0 : row.getTotalWatchCount());
        sample.setAvgScore(row.getAvgScore() == null ? 0 : round(row.getAvgScore()));
        return sample;
    }

    private double computeCorrelation(List<Double> xs, List<Double> ys) {
        if (xs.size() != ys.size() || xs.size() < 2) {
            return 0;
        }
        int n = xs.size();
        double meanX = xs.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double meanY = ys.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double numerator = 0;
        double sumSqX = 0;
        double sumSqY = 0;
        for (int i = 0; i < n; i++) {
            double dx = xs.get(i) - meanX;
            double dy = ys.get(i) - meanY;
            numerator += dx * dy;
            sumSqX += dx * dx;
            sumSqY += dy * dy;
        }
        double denominator = Math.sqrt(sumSqX) * Math.sqrt(sumSqY);
        if (denominator == 0) {
            return 0;
        }
        return BigDecimal.valueOf(numerator / denominator)
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Long resolveCourseId(Long courseId, Long videoId) {
        if (courseId != null) {
            return courseId;
        }
        if (videoId != null) {
            Video video = videoMapper.findById(videoId);
            return video != null ? video.getCourseId() : null;
        }
        return null;
    }
}
