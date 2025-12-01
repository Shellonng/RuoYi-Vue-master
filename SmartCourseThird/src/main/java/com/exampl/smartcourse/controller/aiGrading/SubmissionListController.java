package com.exampl.smartcourse.controller.aiGrading;

import com.exampl.smartcourse.common.PageResult;
import com.exampl.smartcourse.dto.aiGrading.SubmissionListRequest;
import com.exampl.smartcourse.service.aiGrading.SubmissionListService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-grading")
@RequiredArgsConstructor
public class SubmissionListController {

    private final SubmissionListService submissionListService;

    @GetMapping("/submissions")
    public PageResult list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "order", required = false) String order
    ) {
        SubmissionListRequest req = new SubmissionListRequest();
        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSortBy(sortBy);
        req.setOrder(order);
        return submissionListService.list(req);
    }
}