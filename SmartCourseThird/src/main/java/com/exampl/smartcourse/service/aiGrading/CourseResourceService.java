package com.exampl.smartcourse.service.aiGrading;

import com.exampl.smartcourse.entity.aiGrading.CourseResource;
import com.exampl.smartcourse.mapper.aiGrading.CourseResourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseResourceService {

    private final CourseResourceMapper courseResourceMapper;

    public List<CourseResource> listByCourseId(Long courseId) {
        return courseResourceMapper.selectByCourseId(courseId);
    }
}