package com.exampl.smartcourse.analysis.service;

import com.exampl.smartcourse.analysis.dto.PerformanceAnalysisResponse;
import com.exampl.smartcourse.common.Result;

public interface AnalysisService {

    /**
     * 基于知识点的成绩分析。
     */
    Result<PerformanceAnalysisResponse> analyzePerformance(Long courseId,
                                                           Long studentUserId,
                                                           Double weakThreshold);
}
