package com.exampl.smartcourse.controller.aiGrading;

import com.exampl.smartcourse.dto.aiGrading.PendingListResult;
import com.exampl.smartcourse.dto.aiGrading.SubmissionDetailResponse;
import com.exampl.smartcourse.service.aiGrading.ProcessingFailedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai-grading")
@RequiredArgsConstructor
public class ProcessingFailedController {

    private final ProcessingFailedService processingFailedService;

    @GetMapping("/unresolved")
    public PendingListResult list(@RequestParam(value = "courseId", required = false) Long courseId) {
        List<SubmissionDetailResponse> data = processingFailedService.listUnresolved(courseId);
        PendingListResult resp = new PendingListResult();
        resp.setCode(200);
        resp.setMessage("success");
        resp.setData(data);
        return resp;
    }
}