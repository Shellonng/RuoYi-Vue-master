package com.exampl.smartcourse.mapper.smartpaper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exampl.smartcourse.dto.smartpaper.request.QueryQuestionRequest;
import com.exampl.smartcourse.entity.smartpaper.Question;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 题目Mapper接口
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 分页查询题目
     *
     * @param page    分页对象
     * @param request 查询条件
     * @return 分页结果
     */
    IPage<Question> selectQuestionPage(Page<Question> page, @Param("req") QueryQuestionRequest request);

    /**
     * 根据条件随机选择题目
     *
     * @param courseId      课程ID
     * @param chapterIds    章节ID列表
     * @param questionType  题型
     * @param difficulty    难度
     * @param knowledgePoints 知识点列表
     * @param excludeIds    排除的题目ID列表
     * @param limit         数量限制
     * @return 题目列表
     */
    List<Question> selectRandomQuestions(@Param("courseId") Long courseId,
                                         @Param("chapterIds") List<Long> chapterIds,
                                         @Param("questionType") String questionType,
                                         @Param("difficulty") Integer difficulty,
                                         @Param("knowledgePoints") List<String> knowledgePoints,
                                         @Param("excludeIds") List<Long> excludeIds,
                                         @Param("limit") Integer limit);

    /**
     * 统计题目数量（按题型）
     *
     * @param courseId 课程ID
     * @param chapterIds 章节ID列表
     * @return 统计结果
     */
    List<Map<String, Object>> countByQuestionType(@Param("courseId") Long courseId,
                                                   @Param("chapterIds") List<Long> chapterIds);

    /**
     * 统计题目数量（按难度）
     *
     * @param courseId 课程ID
     * @param chapterIds 章节ID列表
     * @return 统计结果
     */
    List<Map<String, Object>> countByDifficulty(@Param("courseId") Long courseId,
                                                 @Param("chapterIds") List<Long> chapterIds);

    /**
     * 统计题目数量（按知识点）
     *
     * @param courseId 课程ID
     * @param chapterIds 章节ID列表
     * @return 统计结果
     */
    List<Map<String, Object>> countByKnowledgePoint(@Param("courseId") Long courseId,
                                                     @Param("chapterIds") List<Long> chapterIds);
}
