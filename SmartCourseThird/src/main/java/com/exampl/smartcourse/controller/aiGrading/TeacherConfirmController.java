package com.exampl.smartcourse.controller.aiGrading;

import com.exampl.smartcourse.dto.aiGrading.GradingModificationRequest;
import com.exampl.smartcourse.dto.aiGrading.TeacherConfirmResult;
import com.exampl.smartcourse.entity.aiGrading.AiGrading;
import com.exampl.smartcourse.service.aiGrading.TeacherConfirmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-grading")
@RequiredArgsConstructor
public class TeacherConfirmController {

    private final TeacherConfirmService teacherConfirmService;

    @PostMapping("/grading/{submissionId}/confirm")
    public ResponseEntity<TeacherConfirmResult> confirm(
            @PathVariable("submissionId") Long submissionId,
            @RequestParam("teacherId") Long teacherId,
            @RequestBody GradingModificationRequest request
    ) {
        AiGrading data = teacherConfirmService.confirm(submissionId, teacherId, request);
        TeacherConfirmResult resp = new TeacherConfirmResult();
        resp.setCode(200);
        resp.setMessage("success");
        resp.setData(data);
        return ResponseEntity.ok(resp);
    }
}
