package com.exampl.smartcourse.service.aiGrading;

import com.exampl.smartcourse.common.PageResult;
import com.exampl.smartcourse.entity.aiGrading.Assignment;
import com.exampl.smartcourse.mapper.aiGrading.AssignmentMapper;
import com.exampl.smartcourse.mapper.aiGrading.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentByStudentService {

    private final AssignmentMapper assignmentMapper;
    private final UserMapper userMapper;

    public PageResult list(Long studentUserId, Integer page, Integer pageSize, String sortBy, String order) {
        int p = page != null && page > 0 ? page : 1;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;
        String ord = order != null && order.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        String sort = sortBy != null ? sortBy : "createTime";
        String column = mapSortColumn(sort);
        int offset = (p - 1) * ps;
        long total = assignmentMapper.countByStudentUserId(studentUserId);
        List<Assignment> data;
        if ("publisherRealName".equals(sort)) {
            data = assignmentMapper.selectByStudentUserIdOrderByPublisherRealName(studentUserId, ord, offset, ps);
        } else {
            String orderClause = column + " " + ord;
            data = assignmentMapper.selectByStudentUserIdPaged(studentUserId, orderClause, offset, ps);
        }
        for (Assignment a : data) {
            Long uid = a.getPublisherUserId();
            if (uid != null) {
                var u = userMapper.selectById(uid);
                if (u != null) a.setPublisherRealName(u.getRealName());
            }
        }
        return new PageResult(200, "success", total, data);
    }

    private String mapSortColumn(String sort) {
        switch (sort) {
            case "id": return "a.id";
            case "title": return "a.title";
            case "courseId": return "a.course_id";
            case "publisherUserId": return "a.publisher_user_id";
            case "startTime": return "a.start_time";
            case "endTime": return "a.end_time";
            case "createTime": return "a.create_time";
            case "updateTime": return "a.update_time";
            case "totalScore": return "a.total_score";
            case "duration": return "a.duration";
            case "status": return "a.status";
            default: return "a.create_time";
        }
    }
}