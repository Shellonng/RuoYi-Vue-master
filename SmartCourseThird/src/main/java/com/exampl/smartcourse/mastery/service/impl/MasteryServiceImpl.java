package com.exampl.smartcourse.mastery.service.impl;

import com.exampl.smartcourse.common.ErrorCode;
import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.mastery.dto.ClassMasteryResponse;
import com.exampl.smartcourse.mastery.dto.KPMasteryAggDTO;
import com.exampl.smartcourse.mastery.dto.MasteryDTO;
import com.exampl.smartcourse.mastery.mapper.MasteryMapper;
import com.exampl.smartcourse.mastery.model.StudentKPMasteryDO;
import com.exampl.smartcourse.mastery.service.MasteryService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MasteryServiceImpl implements MasteryService {

    private static final double DEFAULT_LOW_THRESHOLD = 0.6D;

    private static final int SCALE = 4;

    private final MasteryMapper masteryMapper;

    public MasteryServiceImpl(MasteryMapper masteryMapper) {
        this.masteryMapper = masteryMapper;
    }

    @Override
    public Result<List<MasteryDTO>> getStudentMastery(Long courseId, Long studentUserId) {
        if (studentUserId == null || studentUserId <= 0) {
            return new Result<>(ErrorCode.INVALID_PARAM, "studentUserId 不合法", null);
        }
        List<StudentKPMasteryDO> data = masteryMapper.listStudentMastery(courseId, studentUserId);
        if (CollectionUtils.isEmpty(data)) {
            return new Result<>(ErrorCode.SUCCESS, "暂无掌握度数据", Collections.emptyList());
        }
        List<MasteryDTO> results = data.stream().map(this::convert).collect(Collectors.toList());
        return new Result<>(ErrorCode.SUCCESS, "OK", results);
    }

    @Override
    public Result<ClassMasteryResponse> getClassMastery(Long courseId, Long classId, Double lowScoreThreshold) {
        if (classId == null || classId <= 0) {
            return new Result<>(ErrorCode.INVALID_PARAM, "classId 不合法", null);
        }
        double threshold = lowScoreThreshold != null && lowScoreThreshold > 0 ? lowScoreThreshold : DEFAULT_LOW_THRESHOLD;
        List<StudentKPMasteryDO> data = masteryMapper.listClassMastery(courseId, classId);
        if (CollectionUtils.isEmpty(data)) {
            ClassMasteryResponse empty = new ClassMasteryResponse();
            empty.setKpStats(Collections.emptyList());
            empty.setWeakKps(Collections.emptyList());
            empty.setSuggestions(Collections.emptyList());
            return new Result<>(ErrorCode.SUCCESS, "暂无班级掌握度数据", empty);
        }
        Map<Long, List<StudentKPMasteryDO>> groupByKp = data.stream()
                .filter(item -> item.getMasteryScore() != null)
                .collect(Collectors.groupingBy(StudentKPMasteryDO::getKpId));
        List<KPMasteryAggDTO> aggList = new ArrayList<>();
        for (Map.Entry<Long, List<StudentKPMasteryDO>> entry : groupByKp.entrySet()) {
            Long kpId = entry.getKey();
            List<StudentKPMasteryDO> values = entry.getValue();
            DoubleSummaryStatistics stats = values.stream()
                    .filter(item -> item.getMasteryScore() != null)
                    .mapToDouble(StudentKPMasteryDO::getMasteryScore)
                    .summaryStatistics();
            double avg = round(stats.getAverage());
            double variance = 0D;
            if (values.size() > 1) {
                double mean = stats.getAverage();
                variance = values.stream()
                        .filter(item -> item.getMasteryScore() != null)
                        .mapToDouble(item -> {
                            double diff = item.getMasteryScore() - mean;
                            return diff * diff;
                        })
                        .sum() / values.size();
            }
            double stddev = round(Math.sqrt(variance));
            int lowCount = (int) values.stream()
                    .filter(item -> item.getMasteryScore() != null && item.getMasteryScore() < threshold)
                    .count();
            KPMasteryAggDTO agg = new KPMasteryAggDTO();
            agg.setKpId(kpId);
            agg.setKpTitle(values.get(0).getKpTitle());
            agg.setAverageScore(avg);
            agg.setStandardDeviation(stddev);
            agg.setLowScoreCount(lowCount);
            agg.setStudentCount(values.size());
            double lowRate = values.isEmpty() ? 0D : round((lowCount * 100D) / values.size());
            agg.setLowScoreRate(lowRate);
            agg.setDiagnosis(buildDiagnosis(avg, lowRate));
            aggList.add(agg);
        }
        ClassMasteryResponse response = new ClassMasteryResponse();
        List<KPMasteryAggDTO> ranking = aggList.stream()
                .sorted((a, b) -> Double.compare(
                        Objects.requireNonNullElse(b.getAverageScore(), 0D),
                        Objects.requireNonNullElse(a.getAverageScore(), 0D)))
                .collect(Collectors.toList());
        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setRank(i + 1);
        }
        List<KPMasteryAggDTO> weakKps = ranking.stream()
                .filter(item -> item.getAverageScore() != null && item.getAverageScore() < threshold)
                .sorted(Comparator.comparing(item -> Objects.requireNonNullElse(item.getAverageScore(), 0D)))
                .limit(5)
                .collect(Collectors.toList());
        DoubleSummaryStatistics overallStats = data.stream()
                .filter(item -> item.getMasteryScore() != null)
                .mapToDouble(StudentKPMasteryDO::getMasteryScore)
                .summaryStatistics();
        response.setKpStats(ranking);
        response.setWeakKps(weakKps);
        response.setOverallAverageScore(overallStats.getCount() == 0 ? null : round(overallStats.getAverage()));
        response.setSuggestions(buildSuggestions(ranking, weakKps, threshold));
        return new Result<>(ErrorCode.SUCCESS, "OK", response);
    }

    private MasteryDTO convert(StudentKPMasteryDO source) {
        MasteryDTO dto = new MasteryDTO();
        dto.setKpId(source.getKpId());
        dto.setKpTitle(source.getKpTitle());
        dto.setMasteryScore(source.getMasteryScore());
        dto.setMasteryStatus(source.getMasteryStatus());
        dto.setTrend(source.getTrend());
        dto.setUpdateTime(source.getUpdateTime());
        if (source.getMasteryScore() != null) {
            dto.setAccuracyRate(round(source.getMasteryScore() * 100));
            dto.setMasteryDescription(describeMastery(source.getMasteryScore()));
        }
        return dto;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(SCALE, RoundingMode.HALF_UP).doubleValue();
    }

    private String describeMastery(double score) {
        if (score >= 0.85) {
            return "掌握扎实，建议开展进阶拓展";
        }
        if (score >= 0.7) {
            return "基本掌握，可安排巩固型练习";
        }
        if (score >= 0.5) {
            return "掌握偏弱，需要复习基础知识";
        }
        return "掌握薄弱，建议针对性辅导";
    }

    private String buildDiagnosis(Double avgScore, Double lowRate) {
        if (avgScore == null) {
            return "暂无数据";
        }
        if (avgScore >= 0.85) {
            return "整体优秀，可加入拔尖训练";
        }
        if (avgScore >= 0.7) {
            if (lowRate != null && lowRate > 20) {
                return "大部分掌握稳定，但仍有学生掉队";
            }
            return "掌握良好，保持练习频率";
        }
        if (avgScore >= 0.5) {
            return "掌握不均衡，需进行针对性复盘";
        }
        return "多数学生薄弱，应安排系统化讲解";
    }

    private List<String> buildSuggestions(List<KPMasteryAggDTO> ranking,
                                          List<KPMasteryAggDTO> weakKps,
                                          double threshold) {
        List<String> suggestions = new ArrayList<>();
        if (!ranking.isEmpty()) {
            KPMasteryAggDTO top = ranking.get(0);
            suggestions.add(String.format("掌握领先：%s 平均掌握度 %.1f%%，适合布置拔尖练习。",
                    top.getKpTitle(), top.getAverageScore() * 100));
        }
        if (!weakKps.isEmpty()) {
            String weakNames = weakKps.stream()
                    .map(KPMasteryAggDTO::getKpTitle)
                    .collect(Collectors.joining("、"));
            suggestions.add(String.format("薄弱关注：%s 掌握度低于 %.0f%%，建议课堂重点讲解。",
                    weakNames, threshold * 100));
        } else {
            suggestions.add("整体掌握稳定，可结合学习行为数据做精细化分层。");
        }
        return suggestions;
    }
}
