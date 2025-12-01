package com.exampl.smartcourse.mastery.controller;

import com.exampl.smartcourse.common.ErrorCode;
import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.mastery.dto.ClassMasteryResponse;
import com.exampl.smartcourse.mastery.dto.MasteryDTO;
import com.exampl.smartcourse.mastery.service.MasteryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mastery")
public class MasteryController {

    private final MasteryService masteryService;

    public MasteryController(MasteryService masteryService) {
        this.masteryService = masteryService;
    }

    @GetMapping("/student")
    public Result<List<MasteryDTO>> getStudentMastery(@RequestParam(value = "courseId", required = false) Long courseId,
                                                      @RequestParam(value = "studentUserId") Long studentUserId) {
        if (studentUserId == null || studentUserId <= 0) {
            return new Result<>(ErrorCode.INVALID_PARAM, "studentUserId 不合法", null);
        }
        return masteryService.getStudentMastery(courseId, studentUserId);
    }

    @GetMapping("/class")
    public Result<ClassMasteryResponse> getClassMastery(@RequestParam(value = "courseId", required = false) Long courseId,
                                                        @RequestParam(value = "classId") Long classId,
                                                        @RequestParam(value = "lowScoreThreshold", required = false) Double lowScoreThreshold) {
        if (classId == null || classId <= 0) {
            return new Result<>(ErrorCode.INVALID_PARAM, "classId 不合法", null);
        }
        return masteryService.getClassMastery(courseId, classId, lowScoreThreshold);
    }
}
