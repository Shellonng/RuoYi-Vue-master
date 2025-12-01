package com.exampl.smartcourse.analysis.service.impl;

import com.exampl.smartcourse.analysis.dto.PerformanceAnalysisResponse;
import com.exampl.smartcourse.analysis.mapper.AnalysisMapper;
import com.exampl.smartcourse.analysis.model.KpScoreAggDO;
import com.exampl.smartcourse.analysis.model.ScoreTrendDO;
import com.exampl.smartcourse.analysis.service.AnalysisService;
import com.exampl.smartcourse.common.ErrorCode;
import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.mastery.mapper.MasteryMapper;
import com.exampl.smartcourse.mastery.model.StudentKPMasteryDO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    /**
     * 默认薄弱阈值：百分比 60。
     */
    private static final double DEFAULT_WEAK_THRESHOLD = 60D;
    private static final int SCALE = 4;

    private final AnalysisMapper analysisMapper;
    private final MasteryMapper masteryMapper;

    public AnalysisServiceImpl(AnalysisMapper analysisMapper, MasteryMapper masteryMapper) {
        this.analysisMapper = analysisMapper;
        this.masteryMapper = masteryMapper;
    }

    @Override
    public Result<PerformanceAnalysisResponse> analyzePerformance(Long courseId,
                                                                  Long studentUserId,
                                                                  Double weakThreshold) {
        if (studentUserId == null || studentUserId <= 0) {
            return new Result<>(ErrorCode.INVALID_PARAM, "studentUserId 不合法", null);
        }
        if (courseId == null || courseId <= 0) {
            return new Result<>(ErrorCode.INVALID_PARAM, "courseId 不合法", null);
        }
        double threshold = weakThreshold != null && weakThreshold > 0 ? weakThreshold : DEFAULT_WEAK_THRESHOLD;
        List<KpScoreAggDO> studentScores = analysisMapper.listStudentKpScores(courseId, studentUserId);
        List<StudentKPMasteryDO> masteryList = masteryMapper.listStudentMastery(courseId, studentUserId);
        Map<Long, StudentKPMasteryDO> masteryMap = masteryList == null ? Collections.emptyMap() :
                masteryList.stream().collect(Collectors.toMap(StudentKPMasteryDO::getKpId, item -> item, (a, b) -> a));
        Map<Long, KpScoreAggDO> studentScoreMap = studentScores == null ? Collections.emptyMap() :
                studentScores.stream().filter(item -> item.getKpId() != null)
                        .collect(Collectors.toMap(KpScoreAggDO::getKpId, item -> item, (a, b) -> a));

        Set<Long> kpIds = new HashSet<>();
        kpIds.addAll(studentScoreMap.keySet());
        kpIds.addAll(masteryMap.keySet());

        List<PerformanceAnalysisResponse.KnowledgeScoreItem> knowledgeItems = new ArrayList<>();
        for (Long kpId : kpIds) {
            PerformanceAnalysisResponse.KnowledgeScoreItem item = new PerformanceAnalysisResponse.KnowledgeScoreItem();
            item.setKpId(kpId);
            StudentKPMasteryDO mastery = masteryMap.get(kpId);
            KpScoreAggDO student = studentScoreMap.get(kpId);
            if (mastery != null) {
                item.setKpTitle(mastery.getKpTitle());
                item.setMasteryScore(roundToPercent(mastery.getMasteryScore()));
                item.setTrend(mastery.getTrend());
            } else if (student != null) {
                item.setKpTitle(student.getKpTitle());
            }
            if (student != null && student.getScoreRatio() != null) {
                item.setScoreRatio(roundToPercent(student.getScoreRatio()));
            }
            knowledgeItems.add(item);
        }
        knowledgeItems.sort((a, b) -> Long.compare(
                Objects.requireNonNullElse(a.getKpId(), 0L),
                Objects.requireNonNullElse(b.getKpId(), 0L)));

        List<PerformanceAnalysisResponse.KnowledgeScoreItem> weak = knowledgeItems.stream()
                .filter(item -> {
                    Double basis = item.getScoreRatio() != null ? item.getScoreRatio() : item.getMasteryScore();
                    return basis != null && basis < threshold;
                })
                .sorted((a, b) -> Double.compare(
                        Objects.requireNonNullElse(a.getScoreRatio(), Objects.requireNonNullElse(a.getMasteryScore(), 0D)),
                        Objects.requireNonNullElse(b.getScoreRatio(), Objects.requireNonNullElse(b.getMasteryScore(), 0D))))
                .limit(5)
                .collect(Collectors.toList());

        PerformanceAnalysisResponse response = new PerformanceAnalysisResponse();
        response.setKnowledgeScores(knowledgeItems);
        response.setWeakKnowledge(weak);
        response.setScoreTrend(convertTrend(analysisMapper.listScoreTrend(courseId, studentUserId)));

        response.setOverallAverageScore(calculateAverage(knowledgeItems, PerformanceAnalysisResponse.KnowledgeScoreItem::getScoreRatio));
        response.setSuggestions(buildSuggestions(knowledgeItems, weak, threshold));

        return new Result<>(ErrorCode.SUCCESS, "OK", response);
    }

    private List<PerformanceAnalysisResponse.ScoreTrendPoint> convertTrend(List<ScoreTrendDO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(item -> {
            PerformanceAnalysisResponse.ScoreTrendPoint point = new PerformanceAnalysisResponse.ScoreTrendPoint();
            point.setDate(item.getTimestamp());
            point.setScoreRatio(item.getScoreRatio() == null ? null : roundToPercent(item.getScoreRatio()));
            return point;
        }).collect(Collectors.toList());
    }

    private Double calculateAverage(List<PerformanceAnalysisResponse.KnowledgeScoreItem> items,
                                    java.util.function.Function<PerformanceAnalysisResponse.KnowledgeScoreItem, Double> getter) {
        DoubleSummaryStatistics stats = items.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();
        if (stats.getCount() == 0) {
            return null;
        }
        return round(stats.getAverage());
    }

    private List<String> buildSuggestions(List<PerformanceAnalysisResponse.KnowledgeScoreItem> items,
                                          List<PerformanceAnalysisResponse.KnowledgeScoreItem> weak,
                                          double threshold) {
        if (CollectionUtils.isEmpty(items)) {
            return Collections.singletonList("暂无成绩数据");
        }
        List<String> suggestions = new ArrayList<>();
        PerformanceAnalysisResponse.KnowledgeScoreItem best = items.stream()
                .filter(item -> item.getScoreRatio() != null)
                .max((a, b) -> Double.compare(a.getScoreRatio(), b.getScoreRatio()))
                .orElse(null);
        if (best != null) {
            suggestions.add(String.format("表现亮点：%s 得分率 %.1f%%，可安排进阶练习。",
                    best.getKpTitle(), best.getScoreRatio()));
        }
        if (!CollectionUtils.isEmpty(weak)) {
            String weakNames = weak.stream()
                    .map(PerformanceAnalysisResponse.KnowledgeScoreItem::getKpTitle)
                    .collect(Collectors.joining("、"));
            suggestions.add(String.format("薄弱知识点：%s 低于 %.0f%%，建议重点讲解与针对性练习。",
                    weakNames, threshold));
        } else {
            suggestions.add("整体掌握平稳，可结合趋势数据做分层拓展。");
        }
        return suggestions;
    }

    private Double round(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(SCALE, RoundingMode.HALF_UP).doubleValue();
    }

    private Double roundToPercent(Double value) {
        if (value == null) {
            return null;
        }
        return round(value * 100);
    }
}
