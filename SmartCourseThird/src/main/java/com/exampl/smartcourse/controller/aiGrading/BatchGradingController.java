package com.exampl.smartcourse.controller.aiGrading;

import com.exampl.smartcourse.dto.aiGrading.TriggerRequest;
import com.exampl.smartcourse.dto.aiGrading.TriggerResponse;
import com.exampl.smartcourse.dto.aiGrading.TriggerResult;
import com.exampl.smartcourse.service.aiGrading.GradingTriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-grading")
@RequiredArgsConstructor
public class BatchGradingController {

    private final GradingTriggerService gradingTriggerService;

    @PostMapping("/grading/batch")
    public ResponseEntity<TriggerResult> batch(@RequestBody TriggerRequest request) {
        TriggerResponse resp = gradingTriggerService.trigger(request);
        TriggerResult result = new TriggerResult();
        result.setCode(200);
        result.setMessage("success");
        result.setData(resp);
        return ResponseEntity.ok(result);
    }
}