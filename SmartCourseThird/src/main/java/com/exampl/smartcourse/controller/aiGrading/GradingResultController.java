package com.exampl.smartcourse.controller.aiGrading;

import com.exampl.smartcourse.dto.aiGrading.GradingResultResponse;
import com.exampl.smartcourse.dto.aiGrading.GradingResultResult;
import com.exampl.smartcourse.service.aiGrading.GradingResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-grading")
@RequiredArgsConstructor
public class GradingResultController {

    private final GradingResultService gradingResultService;

    @GetMapping("/grading/{submissionId}/result")
    public ResponseEntity<GradingResultResult> getResult(@PathVariable("submissionId") Long submissionId) {
        GradingResultResponse data = gradingResultService.getBySubmissionId(submissionId);
        GradingResultResult resp = new GradingResultResult();
        resp.setCode(200);
        resp.setMessage("success");
        resp.setData(data);
        return ResponseEntity.ok(resp);
    }
}
