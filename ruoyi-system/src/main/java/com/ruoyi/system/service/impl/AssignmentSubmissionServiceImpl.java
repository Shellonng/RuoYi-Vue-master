package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AssignmentSubmission;
import com.ruoyi.system.mapper.AssignmentSubmissionMapper;
import com.ruoyi.system.service.IAssignmentSubmissionService;
import com.ruoyi.system.utils.BusinessUserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 作业提交记录Service业务层处理
 */
@Service
public class AssignmentSubmissionServiceImpl implements IAssignmentSubmissionService {

    @Autowired
    private AssignmentSubmissionMapper assignmentSubmissionMapper;

    @Override
    public List<AssignmentSubmission> selectAssignmentSubmissionList(AssignmentSubmission assignmentSubmission) {
        return assignmentSubmissionMapper.selectAssignmentSubmissionList(assignmentSubmission);
    }

    @Override
    public AssignmentSubmission selectAssignmentSubmissionById(Long id) {
        return assignmentSubmissionMapper.selectAssignmentSubmissionById(id);
    }

    @Override
    public int updateAssignmentSubmission(AssignmentSubmission assignmentSubmission) {
        assignmentSubmission.setUpdateTime(new Date());
        return assignmentSubmissionMapper.updateAssignmentSubmission(assignmentSubmission);
    }

    @Override
    public int gradeAssignmentSubmission(AssignmentSubmission assignmentSubmission) {
        AssignmentSubmission existing = assignmentSubmissionMapper.selectAssignmentSubmissionById(assignmentSubmission.getId());
        if (existing == null) {
            return 0;
        }

        assignmentSubmission.setStatus(assignmentSubmission.getStatus() != null ? assignmentSubmission.getStatus() : 2);
        assignmentSubmission.setGradeTime(new Date());
        assignmentSubmission.setGradedBy(BusinessUserUtils.getCurrentBusinessUserId());
        assignmentSubmission.setUpdateTime(new Date());

        return assignmentSubmissionMapper.updateAssignmentSubmission(assignmentSubmission);
    }
}
