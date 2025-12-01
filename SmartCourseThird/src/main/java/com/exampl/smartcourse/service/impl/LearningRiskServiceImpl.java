package com.exampl.smartcourse.service.impl;

import com.exampl.smartcourse.dto.StudentBehaviorAggregate;
import com.exampl.smartcourse.dto.StudentKpMasteryRecord;
import com.exampl.smartcourse.dto.response.KpRiskAggregate;
import com.exampl.smartcourse.dto.response.RiskItem;
import com.exampl.smartcourse.dto.response.RiskReportResponse;
import com.exampl.smartcourse.dto.response.StudentRiskResponse;
import com.exampl.smartcourse.mapper.StudentKpMasteryMapper;
import com.exampl.smartcourse.mapper.VideoLearningBehaviorMapper;
import com.exampl.smartcourse.service.LearningRiskService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LearningRiskServiceImpl implements LearningRiskService {

    private static final Map<String, Integer> RISK_LEVEL_WEIGHT = Map.of(
            "HIGH", 3,
            "MEDIUM", 2,
            "LOW", 1
    );

    private final StudentKpMasteryMapper masteryMapper;
    private final VideoLearningBehaviorMapper behaviorMapper;

    public LearningRiskServiceImpl(StudentKpMasteryMapper masteryMapper,
                                   VideoLearningBehaviorMapper behaviorMapper) {
        this.masteryMapper = masteryMapper;
        this.behaviorMapper = behaviorMapper;
    }

    @Override
    public StudentRiskResponse evaluateStudentRisks(Long courseId, Long studentId, Long kpId) {
        List<StudentKpMasteryRecord> masteryRecords = masteryMapper.selectMastery(courseId, studentId, kpId);
        List<StudentBehaviorAggregate> behaviorAggregates = behaviorMapper.aggregateBehavior(courseId, studentId);
        Map<String, StudentBehaviorAggregate> behaviorMap = behaviorAggregates.stream()
                .collect(Collectors.toMap(
                        item -> buildKey(item.getStudentId(), item.getCourseId()),
                        item -> item,
                        (a, b) -> a
                ));

        List<RiskItem> risks = new ArrayList<>();
        for (StudentKpMasteryRecord record : masteryRecords) {
            StudentBehaviorAggregate aggregate = behaviorMap.get(buildKey(record.getStudentId(), record.getCourseId()));
            RiskItem riskItem = evaluateRisk(record, aggregate);
            if (riskItem != null) {
                risks.add(riskItem);
            }
        }

        risks.sort((a, b) -> {
            int compareLevel = Integer.compare(
                    RISK_LEVEL_WEIGHT.getOrDefault(b.getRiskLevel(), 0),
                    RISK_LEVEL_WEIGHT.getOrDefault(a.getRiskLevel(), 0)
            );
            if (compareLevel != 0) {
                return compareLevel;
            }
            return Double.compare(
                    b.getMasteryScore() == null ? 0 : b.getMasteryScore(),
                    a.getMasteryScore() == null ? 0 : a.getMasteryScore()
            );
        });

        StudentRiskResponse response = new StudentRiskResponse();
        response.setRisks(risks);
        response.setTotalRisks(risks.size());
        response.setRiskTypeCount(risks.stream()
                .filter(item -> item.getRiskType() != null)
                .collect(Collectors.groupingBy(RiskItem::getRiskType, Collectors.counting())));
        response.setRiskLevelCount(risks.stream()
                .filter(item -> item.getRiskLevel() != null)
                .collect(Collectors.groupingBy(RiskItem::getRiskLevel, Collectors.counting())));
        return response;
    }

    @Override
    public RiskReportResponse buildRiskReport(Long courseId) {
        List<StudentKpMasteryRecord> masteryRecords = masteryMapper.selectMastery(courseId, null, null);
        StudentRiskResponse riskResponse = evaluateStudentRisks(courseId, null, null);
        List<RiskItem> risks = riskResponse.getRisks();

        Map<String, Long> riskTypeCount = riskResponse.getRiskTypeCount();
        List<RiskItem> highRiskStudents = risks.stream()
                .filter(item -> Objects.equals("HIGH", item.getRiskLevel()))
                .limit(20)
                .collect(Collectors.toList());

        List<KpRiskAggregate> kpRiskList = buildKpRiskAggregates(masteryRecords);

        long lowMasteryCount = masteryRecords.stream()
                .filter(record -> record.getMasteryScore() != null && record.getMasteryScore() < 60)
                .count();
        double lowMasteryRatio = masteryRecords.isEmpty() ? 0 :
                round((double) lowMasteryCount / masteryRecords.size() * 100);

        RiskReportResponse report = new RiskReportResponse();
        report.setRiskTypeCount(riskTypeCount);
        report.setHighRiskStudents(highRiskStudents);
        report.setKpRiskList(kpRiskList);
        report.setOverallLowMasteryRatio(lowMasteryRatio);
        report.setSummary(buildSummary(riskTypeCount, lowMasteryRatio, kpRiskList));
        return report;
    }

    private String buildSummary(Map<String, Long> riskTypeCount, double lowMasteryRatio, List<KpRiskAggregate> kpRiskList) {
        if (CollectionUtils.isEmpty(riskTypeCount)) {
            return "整体掌握情况良好，暂未发现明显风险。";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("当前约 ").append(lowMasteryRatio).append("% 的知识点掌握度偏低。");
        if (!CollectionUtils.isEmpty(kpRiskList)) {
            builder.append("薄弱知识点集中在：");
            builder.append(kpRiskList.stream()
                    .limit(3)
                    .map(KpRiskAggregate::getKpTitle)
                    .collect(Collectors.joining("、")));
            builder.append("。");
        }
        if (riskTypeCount.containsKey("learning_difficulty")) {
            builder.append("部分学生存在学习困难，建议安排针对性辅导；");
        }
        if (riskTypeCount.containsKey("resource_mismatch")) {
            builder.append("部分学生学习活跃但掌握度低，建议复核资源匹配度；");
        }
        if (riskTypeCount.containsKey("low_engagement")) {
            builder.append("存在学习行为低活跃的学生，需要督促学习进度。");
        }
        return builder.toString();
    }

    private List<KpRiskAggregate> buildKpRiskAggregates(List<StudentKpMasteryRecord> masteryRecords) {
        Map<Long, List<StudentKpMasteryRecord>> groupByKp = masteryRecords.stream()
                .filter(record -> record.getKpId() != null)
                .collect(Collectors.groupingBy(StudentKpMasteryRecord::getKpId));

        List<KpRiskAggregate> aggregates = new ArrayList<>();
        for (Map.Entry<Long, List<StudentKpMasteryRecord>> entry : groupByKp.entrySet()) {
            List<StudentKpMasteryRecord> records = entry.getValue();
            double average = records.stream()
                    .map(StudentKpMasteryRecord::getMasteryScore)
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0);
            double variance = records.stream()
                    .map(StudentKpMasteryRecord::getMasteryScore)
                    .filter(Objects::nonNull)
                    .mapToDouble(score -> Math.pow(score - average, 2))
                    .average()
                    .orElse(0);
            long lowCount = records.stream()
                    .filter(item -> item.getMasteryScore() != null && item.getMasteryScore() < 60)
                    .count();

            KpRiskAggregate aggregate = new KpRiskAggregate();
            aggregate.setKpId(entry.getKey());
            aggregate.setKpTitle(records.get(0).getKpTitle());
            aggregate.setAverageMastery(round(average));
            aggregate.setMasteryStdDev(round(Math.sqrt(variance)));
            aggregate.setLowMasteryCount(lowCount);
            aggregates.add(aggregate);
        }

        aggregates.sort(Comparator.comparingDouble(item -> item.getAverageMastery() == null ? 0 : item.getAverageMastery()));
        return aggregates;
    }

    private RiskItem evaluateRisk(StudentKpMasteryRecord record, StudentBehaviorAggregate behavior) {
        double masteryScore = record.getMasteryScore() == null ? 0 : record.getMasteryScore();
        double completionRate = behavior != null && behavior.getAvgCompletionRate() != null
                ? behavior.getAvgCompletionRate()
                : 0;
        long watchCount = behavior != null && behavior.getTotalWatchCount() != null
                ? behavior.getTotalWatchCount()
                : 0;
        double activityScore = computeActivityScore(completionRate, watchCount);

        String riskType = null;
        String riskLevel = null;
        String suggestion = null;

        if (masteryScore < 50 && activityScore < 40) {
            riskType = "learning_difficulty";
            riskLevel = "HIGH";
            suggestion = "建议教师安排一对一辅导或答疑，加强基础知识巩固。";
        } else if (masteryScore < 50) {
            riskType = "resource_mismatch";
            riskLevel = "MEDIUM";
            suggestion = "学生学习积极但掌握度低，建议复核学习资源与知识点的匹配度。";
        } else if (masteryScore < 70 && activityScore < 40) {
            riskType = "low_engagement";
            riskLevel = "MEDIUM";
            suggestion = "学习行为不活跃，建议督促学生完成课堂资源与视频学习。";
        } else if (masteryScore < 70) {
            riskType = "knowledge_gap";
            riskLevel = "LOW";
            suggestion = "知识点掌握度偏低，建议布置针对性练习巩固。";
        } else if (activityScore < 30) {
            riskType = "inactive";
            riskLevel = "LOW";
            suggestion = "学习活动低迷，请关注学生学习参与度。";
        }

        if (riskType == null) {
            return null;
        }

        RiskItem item = new RiskItem();
        item.setStudentId(record.getStudentId());
        item.setCourseId(record.getCourseId());
        item.setKpId(record.getKpId());
        item.setKpTitle(record.getKpTitle());
        item.setMasteryScore(round(masteryScore));
        item.setMasteryStatus(record.getMasteryStatus());
        item.setAvgCompletionRate(round(completionRate));
        item.setActivityScore(round(activityScore));
        item.setRiskType(riskType);
        item.setRiskLevel(riskLevel);
        item.setSuggestion(suggestion);
        return item;
    }

    private double computeActivityScore(double completionRate, long watchCount) {
        double normalizedWatch = Math.min(watchCount * 20.0, 100.0);
        double score = 0.7 * completionRate + 0.3 * normalizedWatch;
        return round(score);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private String buildKey(Long studentId, Long courseId) {
        return studentId + "_" + courseId;
    }
}

