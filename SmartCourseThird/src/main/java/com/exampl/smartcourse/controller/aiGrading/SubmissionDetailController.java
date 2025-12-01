package com.exampl.smartcourse.controller.aiGrading;

import com.exampl.smartcourse.dto.aiGrading.SubmissionDetailResponse;
import com.exampl.smartcourse.dto.aiGrading.SubmissionDetailResult;
import com.exampl.smartcourse.service.aiGrading.SubmissionDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-grading")
@RequiredArgsConstructor
public class SubmissionDetailController {

    private final SubmissionDetailService submissionDetailService;

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<SubmissionDetailResult> getDetail(@PathVariable("submissionId") Long submissionId) {
        SubmissionDetailResponse data = submissionDetailService.getDetail(submissionId);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        SubmissionDetailResult result = new SubmissionDetailResult();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return ResponseEntity.ok(result);
    }
}
