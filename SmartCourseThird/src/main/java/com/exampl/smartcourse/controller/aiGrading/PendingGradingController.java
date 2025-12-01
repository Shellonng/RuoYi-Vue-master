package com.exampl.smartcourse.controller.aiGrading;

import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.dto.aiGrading.PendingListResult;
import com.exampl.smartcourse.dto.aiGrading.SubmissionDetailResponse;
import com.exampl.smartcourse.service.aiGrading.PendingGradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/ai-grading")
@RequiredArgsConstructor
public class PendingGradingController {

    private final PendingGradingService pendingGradingService;

    @GetMapping("/pending")
    public PendingListResult listPending(
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "assignmentId", required = false) Long assignmentId,
            @RequestParam(value = "studentUserId", required = false) Long studentUserId,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        List<SubmissionDetailResponse> data = pendingGradingService.listPendingSubmissions(courseId, assignmentId, studentUserId, keyword);
        PendingListResult resp = new PendingListResult();
        resp.setCode(200);
        resp.setMessage("success");
        resp.setData(data);
        return resp;
    }
}