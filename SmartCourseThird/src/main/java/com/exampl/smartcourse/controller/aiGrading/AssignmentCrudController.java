package com.exampl.smartcourse.controller.aiGrading;

import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.entity.aiGrading.Assignment;
import com.exampl.smartcourse.service.aiGrading.AssignmentCrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-grading")
@RequiredArgsConstructor
public class AssignmentCrudController {

    private final AssignmentCrudService assignmentCrudService;

    @PostMapping("/assignments")
    public ResponseEntity<Result<Assignment>> create(@RequestBody Assignment assignment) {
        Result<Assignment> r = assignmentCrudService.create(assignment);
        return ResponseEntity.ok(r);
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Result<Boolean>> delete(@PathVariable("id") Long id) {
        Result<Boolean> r = assignmentCrudService.delete(id);
        return ResponseEntity.ok(r);
    }
}