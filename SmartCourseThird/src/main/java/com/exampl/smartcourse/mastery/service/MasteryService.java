package com.exampl.smartcourse.mastery.service;

import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.mastery.dto.ClassMasteryResponse;
import com.exampl.smartcourse.mastery.dto.MasteryDTO;

import java.util.List;

public interface MasteryService {

    /**
     * 获取学生的知识点掌握度列表。
     */
    Result<List<MasteryDTO>> getStudentMastery(Long courseId, Long studentUserId);

    /**
     * 获取班级层面的知识点掌握统计。
     */
    Result<ClassMasteryResponse> getClassMastery(Long courseId, Long classId, Double lowScoreThreshold);
}
