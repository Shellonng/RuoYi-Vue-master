package com.exampl.smartcourse.controller.aiGrading;

import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.entity.aiGrading.AiGrading;
import com.exampl.smartcourse.service.aiGrading.GradingStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-grading")
@RequiredArgsConstructor
public class GradingStatusController {

    private final GradingStatusService gradingStatusService;

    @DeleteMapping("/grading/{submissionId}/latest")
    public ResponseEntity<Result<Boolean>> deleteLatest(@PathVariable("submissionId") Long submissionId) {
        boolean ok = gradingStatusService.deleteLatest(submissionId);
        Result<Boolean> resp = new Result<>(200, "success", ok);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/submissions/mark-graded")
    public ResponseEntity<Result<Integer>> markAllHavingGradingAsGraded() {
        int updated = gradingStatusService.markAllHavingGradingAsGraded();
        Result<Integer> resp = new Result<>(200, "success", updated);
        return ResponseEntity.ok(resp);
    }
}
