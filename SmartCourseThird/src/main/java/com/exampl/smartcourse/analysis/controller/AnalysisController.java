package com.exampl.smartcourse.analysis.controller;

import com.exampl.smartcourse.analysis.dto.PerformanceAnalysisResponse;
import com.exampl.smartcourse.analysis.service.AnalysisService;
import com.exampl.smartcourse.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/performance")
    public Result<PerformanceAnalysisResponse> analyzePerformance(@RequestParam("courseId") Long courseId,
                                                                  @RequestParam("studentUserId") Long studentUserId,
                                                                  @RequestParam(value = "weakThreshold", required = false) Double weakThreshold) {
        return analysisService.analyzePerformance(courseId, studentUserId, weakThreshold);
    }
}
