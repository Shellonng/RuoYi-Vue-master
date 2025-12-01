package com.exampl.smartcourse.service.aiGrading;

import com.exampl.smartcourse.common.PageResult;
import com.exampl.smartcourse.dto.aiGrading.SubmissionListRequest;
import com.exampl.smartcourse.dto.aiGrading.SubmissionStudentItem;
import com.exampl.smartcourse.entity.aiGrading.AssignmentSubmission;
import com.exampl.smartcourse.entity.aiGrading.Student;
import com.exampl.smartcourse.mapper.aiGrading.AssignmentSubmissionMapper;
import com.exampl.smartcourse.mapper.aiGrading.StudentMapper;
import com.exampl.smartcourse.mapper.aiGrading.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionListService {

    private final AssignmentSubmissionMapper submissionMapper;
    private final StudentMapper studentMapper;
    private final UserMapper userMapper;

    public PageResult list(SubmissionListRequest req) {
        int page = req.getPage() != null && req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() != null && req.getPageSize() > 0 ? req.getPageSize() : 10;
        String order = req.getOrder() != null && req.getOrder().equalsIgnoreCase("desc") ? "DESC" : "ASC";
        String sort = req.getSortBy() != null ? req.getSortBy() : "createTime";
        String column = mapSortColumn(sort);
        int offset = (page - 1) * pageSize;
        long total = submissionMapper.countAll();
        List<AssignmentSubmission> rows;
        if ("realName".equals(sort)) {
            rows = submissionMapper.selectPagedOrderByRealName(order, offset, pageSize);
        } else {
            String orderClause = column + " " + order;
            rows = submissionMapper.selectPaged(orderClause, offset, pageSize);
        }
        java.util.List<SubmissionStudentItem> data = new java.util.ArrayList<>();
        for (AssignmentSubmission s : rows) {
            SubmissionStudentItem item = new SubmissionStudentItem();
            item.setSubmission(s);
            Student st = studentMapper.selectByUserId(s.getStudentUserId());
            item.setStudent(st);
            com.exampl.smartcourse.entity.aiGrading.User u = userMapper.selectById(s.getStudentUserId());
            if (u != null) {
                item.setUserId(u.getId());
                item.setRealName(u.getRealName());
            } else {
                item.setUserId(s.getStudentUserId());
            }
            data.add(item);
        }
        return new PageResult(200, "success", total, data);
    }

    private String mapSortColumn(String sort) {
        switch (sort) {
            case "id": return "id";
            case "assignmentId": return "assignment_id";
            case "studentUserId": return "student_user_id";
            case "status": return "status";
            case "score": return "score";
            case "submitTime": return "submit_time";
            case "gradeTime": return "grade_time";
            case "createTime": return "create_time";
            case "updateTime": return "update_time";
            case "fileName": return "file_name";
            default: return "create_time";
        }
    }
}