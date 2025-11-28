package com.ruoyi.system.service;

import com.ruoyi.system.domain.AssignmentSubmission;

import java.util.List;

/**
 * 作业提交记录Service接口
 */
public interface IAssignmentSubmissionService {

    /**
     * 查询作业提交记录列表
     *
     * @param assignmentSubmission 查询条件
     * @return 作业提交记录集合
     */
    List<AssignmentSubmission> selectAssignmentSubmissionList(AssignmentSubmission assignmentSubmission);

    /**
     * 根据ID查询作业提交记录
     *
     * @param id 主键
     * @return 作业提交记录
     */
    AssignmentSubmission selectAssignmentSubmissionById(Long id);

    /**
     * 更新作业提交记录
     *
     * @param assignmentSubmission 更新内容
     * @return 结果
     */
    int updateAssignmentSubmission(AssignmentSubmission assignmentSubmission);

    /**
     * 批改作业提交记录
     *
     * @param assignmentSubmission 批改内容
     * @return 结果
     */
    int gradeAssignmentSubmission(AssignmentSubmission assignmentSubmission);
}
