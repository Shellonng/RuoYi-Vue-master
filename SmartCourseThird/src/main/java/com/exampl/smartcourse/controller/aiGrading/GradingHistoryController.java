package com.exampl.smartcourse.controller.aiGrading;

import com.exampl.smartcourse.dto.aiGrading.GradingHistoryResult;
import com.exampl.smartcourse.entity.aiGrading.AiGrading;
import com.exampl.smartcourse.service.aiGrading.GradingHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai-grading")
@RequiredArgsConstructor
public class GradingHistoryController {

    private final GradingHistoryService gradingHistoryService;

    @GetMapping("/grading/{submissionId}/history")
    public GradingHistoryResult listHistory(@PathVariable("submissionId") Long submissionId) {
        List<AiGrading> data = gradingHistoryService.listHistoryBySubmissionId(submissionId);
        GradingHistoryResult resp = new GradingHistoryResult();
        resp.setCode(200);
        resp.setMessage("success");
        resp.setData(data);
        return resp;
    }
}
