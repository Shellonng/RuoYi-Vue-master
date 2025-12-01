package com.exampl.smartcourse.mapper.smartpaper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exampl.smartcourse.entity.smartpaper.QuestionOption;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目选项Mapper接口
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Mapper
public interface QuestionOptionMapper extends BaseMapper<QuestionOption> {

    /**
     * 批量插入选项
     *
     * @param options 选项列表
     * @return 影响行数
     */
    int batchInsert(@Param("options") List<QuestionOption> options);

    /**
     * 根据题目ID删除选项
     *
     * @param questionId 题目ID
     * @return 影响行数
     */
    int deleteByQuestionId(@Param("questionId") Long questionId);

    /**
     * 根据题目ID列表查询选项
     *
     * @param questionIds 题目ID列表
     * @return 选项列表
     */
    List<QuestionOption> selectByQuestionIds(@Param("questionIds") List<Long> questionIds);
}
