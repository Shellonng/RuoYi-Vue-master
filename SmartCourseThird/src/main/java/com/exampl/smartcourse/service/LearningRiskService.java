package com.exampl.smartcourse.service;

import com.exampl.smartcourse.dto.response.RiskReportResponse;
import com.exampl.smartcourse.dto.response.StudentRiskResponse;

public interface LearningRiskService {

    StudentRiskResponse evaluateStudentRisks(Long courseId, Long studentId, Long kpId);

    RiskReportResponse buildRiskReport(Long courseId);
}

