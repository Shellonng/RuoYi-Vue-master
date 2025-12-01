package com.exampl.smartcourse.mapper.smartpaper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exampl.smartcourse.entity.smartpaper.AssignmentQuestion;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 作业题目关联Mapper接口
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Mapper
public interface AssignmentQuestionMapper extends BaseMapper<AssignmentQuestion> {

    /**
     * 批量插入作业题目关联
     *
     * @param assignmentQuestions 作业题目关联列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<AssignmentQuestion> assignmentQuestions);

    /**
     * 根据作业ID删除关联
     *
     * @param assignmentId 作业ID
     * @return 影响行数
     */
    int deleteByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * 查询作业的题目ID列表
     *
     * @param assignmentId 作业ID
     * @return 题目ID列表
     */
    List<Long> selectQuestionIdsByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * 查询作业题目数量
     *
     * @param assignmentId 作业ID
     * @return 题目数量
     */
    Integer countByAssignmentId(@Param("assignmentId") Long assignmentId);
}
