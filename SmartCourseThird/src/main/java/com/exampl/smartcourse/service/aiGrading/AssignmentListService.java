package com.exampl.smartcourse.service.aiGrading;

import com.exampl.smartcourse.common.PageResult;
import com.exampl.smartcourse.dto.aiGrading.AssignmentListRequest;
import com.exampl.smartcourse.entity.aiGrading.Assignment;
import com.exampl.smartcourse.mapper.aiGrading.AssignmentMapper;
import com.exampl.smartcourse.mapper.aiGrading.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentListService {

    private final AssignmentMapper assignmentMapper;
    private final UserMapper userMapper;

    public PageResult list(AssignmentListRequest req) {
        int page = req.getPage() != null && req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() != null && req.getPageSize() > 0 ? req.getPageSize() : 10;
        String order = req.getOrder() != null && req.getOrder().equalsIgnoreCase("desc") ? "DESC" : "ASC";
        String sort = req.getSortBy() != null ? req.getSortBy() : "create_time";
        String column = mapSortColumn(sort);
        int offset = (page - 1) * pageSize;
        long total = assignmentMapper.countAll();
        List<Assignment> data;
        if ("publisherRealName".equals(sort)) {
            data = assignmentMapper.selectPagedOrderByPublisherRealName(order, offset, pageSize);
        } else {
            String orderClause = column + " " + order;
            data = assignmentMapper.selectPaged(orderClause, offset, pageSize);
        }
        for (Assignment a : data) {
            Long uid = a.getPublisherUserId();
            if (uid != null) {
                com.exampl.smartcourse.entity.aiGrading.User u = userMapper.selectById(uid);
                if (u != null) {
                    a.setPublisherRealName(u.getRealName());
                }
            }
        }
        return new PageResult(200, "success", total, data);
    }

    private String mapSortColumn(String sort) {
        switch (sort) {
            case "id": return "id";
            case "title": return "title";
            case "courseId": return "course_id";
            case "publisherUserId": return "publisher_user_id";
            case "startTime": return "start_time";
            case "endTime": return "end_time";
            case "createTime": return "create_time";
            case "updateTime": return "update_time";
            case "totalScore": return "total_score";
            case "duration": return "duration";
            case "status": return "status";
            default: return "create_time";
        }
    }
}