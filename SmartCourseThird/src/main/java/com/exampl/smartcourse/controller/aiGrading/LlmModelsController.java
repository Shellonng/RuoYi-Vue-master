package com.exampl.smartcourse.controller.aiGrading;

import com.exampl.smartcourse.dto.aiGrading.ModelsResult;
import com.exampl.smartcourse.llmclient.aiGrading.SiliconFlowClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-grading")
@RequiredArgsConstructor
public class LlmModelsController {

    private final SiliconFlowClient siliconFlowClient;

    @GetMapping("/models")
    public ResponseEntity<ModelsResult> listModels() {
        ModelsResult result = new ModelsResult();
        result.setCode(200);
        result.setMessage("success");
        result.setData(siliconFlowClient.listModels());
        return ResponseEntity.ok(result);
    }
}