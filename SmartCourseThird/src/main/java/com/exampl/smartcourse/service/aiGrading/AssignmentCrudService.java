package com.exampl.smartcourse.service.aiGrading;

import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.entity.aiGrading.Assignment;
import com.exampl.smartcourse.mapper.aiGrading.AssignmentMapper;
import com.exampl.smartcourse.mapper.aiGrading.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssignmentCrudService {

    private final AssignmentMapper assignmentMapper;
    private final UserMapper userMapper;

    public Result<Assignment> create(Assignment a) {
        if (a.getCreateTime() == null) a.setCreateTime(LocalDateTime.now());
        if (a.getStatus() == null) a.setStatus(0);
        if (a.getMode() == null) a.setMode("question");
        if (a.getTotalScore() == null) a.setTotalScore(100);
        if (a.getIsDeleted() == null) a.setIsDeleted(false);
        assignmentMapper.insert(a);
        Long uid = a.getPublisherUserId();
        if (uid != null) {
            var u = userMapper.selectById(uid);
            if (u != null) a.setPublisherRealName(u.getRealName());
        }
        return new Result<>(200, "success", a);
    }

    public Result<Boolean> delete(Long id) {
        int r = assignmentMapper.deleteById(id);
        return new Result<>(200, "success", r > 0);
    }
}
