package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AssignmentSubmission;

import java.util.List;

/**
 * 作业提交记录Mapper接口
 */
public interface AssignmentSubmissionMapper {

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
     * @return 影响行数
     */
    int updateAssignmentSubmission(AssignmentSubmission assignmentSubmission);
}
