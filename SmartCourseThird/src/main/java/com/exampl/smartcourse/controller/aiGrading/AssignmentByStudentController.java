package com.exampl.smartcourse.controller.aiGrading;

import com.exampl.smartcourse.common.PageResult;
import com.exampl.smartcourse.service.aiGrading.AssignmentByStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-grading")
@RequiredArgsConstructor
public class AssignmentByStudentController {

    private final AssignmentByStudentService assignmentByStudentService;

    @GetMapping("/assignments/by-student")
    public PageResult listByStudent(
            @RequestParam("studentUserId") Long studentUserId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "order", required = false) String order
    ) {
        return assignmentByStudentService.list(studentUserId, page, pageSize, sortBy, order);
    }
}