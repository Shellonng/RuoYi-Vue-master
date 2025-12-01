package com.exampl.smartcourse.controller;

import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.dto.response.RiskReportResponse;
import com.exampl.smartcourse.dto.response.StudentRiskResponse;
import com.exampl.smartcourse.service.LearningRiskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis/risk")
public class LearningRiskController {

    private final LearningRiskService learningRiskService;

    public LearningRiskController(LearningRiskService learningRiskService) {
        this.learningRiskService = learningRiskService;
    }

    @GetMapping("/list")
    public Result<StudentRiskResponse> listRisks(@RequestParam(required = false) Long courseId,
                                                 @RequestParam(required = false) Long studentId,
                                                 @RequestParam(required = false) Long kpId) {
        return Result.success(learningRiskService.evaluateStudentRisks(courseId, studentId, kpId));
    }

    @GetMapping("/report")
    public Result<RiskReportResponse> riskReport(@RequestParam(required = false) Long courseId) {
        return Result.success(learningRiskService.buildRiskReport(courseId));
    }
}

