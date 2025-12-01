package com.exampl.smartcourse.service.smartpaper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exampl.smartcourse.common.smartpaper.PageResult;
import com.exampl.smartcourse.dto.smartpaper.request.CreateQuestionRequest;
import com.exampl.smartcourse.dto.smartpaper.request.QueryQuestionRequest;
import com.exampl.smartcourse.dto.smartpaper.request.UpdateQuestionRequest;
import com.exampl.smartcourse.dto.smartpaper.response.QuestionDetailResponse;
import com.exampl.smartcourse.dto.smartpaper.response.QuestionResponse;
import com.exampl.smartcourse.entity.smartpaper.Question;

import java.util.List;
import java.util.Map;

/**
 * 题目服务接口
 *
 * @author 开发团队
 * @since 2025-11-17
 */
public interface IQuestionService extends IService<Question> {

    /**
     * 创建题目
     *
     * @param request 创建题目请求
     * @param userId  创建人ID
     * @return 题目ID
     */
    Long createQuestion(CreateQuestionRequest request, Long userId);

    /**
     * 更新题目
     *
     * @param request 更新题目请求
     * @return 是否成功
     */
    Boolean updateQuestion(UpdateQuestionRequest request);

    /**
     * 删除题目
     *
     * @param questionId 题目ID
     * @return 是否成功
     */
    Boolean deleteQuestion(Long questionId);

    /**
     * 批量删除题目
     *
     * @param questionIds 题目ID列表
     * @return 是否成功
     */
    Boolean batchDeleteQuestions(List<Long> questionIds);

    /**
     * 查询题目详情
     *
     * @param questionId 题目ID
     * @return 题目详情
     */
    QuestionDetailResponse getQuestionDetail(Long questionId);

    /**
     * 分页查询题目
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<QuestionResponse> queryQuestions(QueryQuestionRequest request);

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
    List<Question> selectRandomQuestions(Long courseId,
                                         List<Long> chapterIds,
                                         String questionType,
                                         Integer difficulty,
                                         List<String> knowledgePoints,
                                         List<Long> excludeIds,
                                         Integer limit);

    /**
     * 统计题目数量（按题型）
     *
     * @param courseId   课程ID
     * @param chapterIds 章节ID列表
     * @return 统计结果
     */
    List<Map<String, Object>> countByQuestionType(Long courseId, List<Long> chapterIds);

    /**
     * 统计题目数量（按难度）
     *
     * @param courseId   课程ID
     * @param chapterIds 章节ID列表
     * @return 统计结果
     */
    List<Map<String, Object>> countByDifficulty(Long courseId, List<Long> chapterIds);

    /**
     * 统计题目数量（按知识点）
     *
     * @param courseId   课程ID
     * @param chapterIds 章节ID列表
     * @return 统计结果
     */
    List<Map<String, Object>> countByKnowledgePoint(Long courseId, List<Long> chapterIds);

    /**
     * 导入题目（批量创建）
     *
     * @param questions 题目列表
     * @param userId    创建人ID
     * @return 成功导入数量
     */
    Integer importQuestions(List<CreateQuestionRequest> questions, Long userId);
}
