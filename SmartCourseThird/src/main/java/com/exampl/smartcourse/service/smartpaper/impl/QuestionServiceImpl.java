package com.exampl.smartcourse.service.smartpaper.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exampl.smartcourse.common.smartpaper.PageResult;
import com.exampl.smartcourse.dto.smartpaper.request.CreateQuestionRequest;
import com.exampl.smartcourse.dto.smartpaper.request.QueryQuestionRequest;
import com.exampl.smartcourse.dto.smartpaper.request.UpdateQuestionRequest;
import com.exampl.smartcourse.dto.smartpaper.response.QuestionDetailResponse;
import com.exampl.smartcourse.dto.smartpaper.response.QuestionOptionResponse;
import com.exampl.smartcourse.dto.smartpaper.response.QuestionResponse;
import com.exampl.smartcourse.entity.smartpaper.Question;
import com.exampl.smartcourse.entity.smartpaper.QuestionOption;
import com.exampl.smartcourse.mapper.smartpaper.QuestionMapper;
import com.exampl.smartcourse.mapper.smartpaper.QuestionOptionMapper;
import com.exampl.smartcourse.service.smartpaper.IQuestionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题目服务实现类
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {

    private final QuestionOptionMapper questionOptionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuestion(CreateQuestionRequest request, Long userId) {
        log.info("创建题目，请求参数：{}，创建人：{}", request, userId);

        // 创建题目
        Question question = new Question();
        BeanUtils.copyProperties(request, question);
        question.setCreatedBy(userId);

        boolean saved = this.save(question);
        if (!saved) {
            throw new RuntimeException("创建题目失败");
        }

        // 如果有选项，保存选项
        if (!CollectionUtils.isEmpty(request.getOptions())) {
            List<QuestionOption> options = new ArrayList<>();
            for (CreateQuestionRequest.QuestionOptionDTO optionDTO : request.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setQuestionId(question.getId());
                option.setOptionLabel(optionDTO.getOptionLabel());
                option.setOptionText(optionDTO.getOptionText());
                options.add(option);
            }
            questionOptionMapper.batchInsert(options);
        }

        log.info("创建题目成功，题目ID：{}", question.getId());
        return question.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateQuestion(UpdateQuestionRequest request) {
        log.info("更新题目，请求参数：{}", request);

        // 检查题目是否存在
        Question existQuestion = this.getById(request.getId());
        if (existQuestion == null) {
            throw new RuntimeException("题目不存在");
        }

        // 更新题目基本信息（仅更新传入的字段）
        Question question = new Question();
        question.setId(request.getId());
        if (request.getTitle() != null) {
            question.setTitle(request.getTitle());
        }
        if (request.getQuestionType() != null) {
            question.setQuestionType(request.getQuestionType());
        }
        if (request.getDifficulty() != null) {
            question.setDifficulty(request.getDifficulty());
        }
        if (request.getCorrectAnswer() != null) {
            question.setCorrectAnswer(request.getCorrectAnswer());
        }
        if (request.getExplanation() != null) {
            question.setExplanation(request.getExplanation());
        }
        if (request.getKnowledgePoint() != null) {
            question.setKnowledgePoint(request.getKnowledgePoint());
        }
        if (request.getCourseId() != null) {
            question.setCourseId(request.getCourseId());
        }
        if (request.getChapterId() != null) {
            question.setChapterId(request.getChapterId());
        }

        boolean updated = this.updateById(question);
        if (!updated) {
            throw new RuntimeException("更新题目失败");
        }

        // 如果有选项，先删除旧选项，再保存新选项
        if (request.getOptions() != null) {
            questionOptionMapper.deleteByQuestionId(request.getId());

            if (!CollectionUtils.isEmpty(request.getOptions())) {
                List<QuestionOption> options = new ArrayList<>();
                for (CreateQuestionRequest.QuestionOptionDTO optionDTO : request.getOptions()) {
                    QuestionOption option = new QuestionOption();
                    option.setQuestionId(request.getId());
                    option.setOptionLabel(optionDTO.getOptionLabel());
                    option.setOptionText(optionDTO.getOptionText());
                    options.add(option);
                }
                questionOptionMapper.batchInsert(options);
            }
        }

        log.info("更新题目成功，题目ID：{}", request.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteQuestion(Long questionId) {
        log.info("删除题目，题目ID：{}", questionId);

        // 逻辑删除题目
        boolean deleted = this.removeById(questionId);
        if (!deleted) {
            throw new RuntimeException("删除题目失败");
        }

        // 删除关联的选项
        questionOptionMapper.deleteByQuestionId(questionId);

        log.info("删除题目成功，题目ID：{}", questionId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDeleteQuestions(List<Long> questionIds) {
        log.info("批量删除题目，题目ID列表：{}", questionIds);

        if (CollectionUtils.isEmpty(questionIds)) {
            return true;
        }

        // 逻辑删除题目
        boolean deleted = this.removeByIds(questionIds);
        if (!deleted) {
            throw new RuntimeException("批量删除题目失败");
        }

        // 删除关联的选项
        for (Long questionId : questionIds) {
            questionOptionMapper.deleteByQuestionId(questionId);
        }

        log.info("批量删除题目成功，删除数量：{}", questionIds.size());
        return true;
    }

    @Override
    public QuestionDetailResponse getQuestionDetail(Long questionId) {
        log.info("查询题目详情，题目ID：{}", questionId);

        // 查询题目
        Question question = this.getById(questionId);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }

        // 转换为响应对象
        QuestionDetailResponse response = new QuestionDetailResponse();
        BeanUtils.copyProperties(question, response);

        // 查询选项
        LambdaQueryWrapper<QuestionOption> optionWrapper = new LambdaQueryWrapper<>();
        optionWrapper.eq(QuestionOption::getQuestionId, questionId);
        List<QuestionOption> options = questionOptionMapper.selectList(optionWrapper);

        if (!CollectionUtils.isEmpty(options)) {
            List<QuestionOptionResponse> optionResponses = options.stream()
                    .map(option -> {
                        QuestionOptionResponse optionResponse = new QuestionOptionResponse();
                        BeanUtils.copyProperties(option, optionResponse);
                        return optionResponse;
                    })
                    .collect(Collectors.toList());
            response.setOptions(optionResponses);
        }

        return response;
    }

    @Override
    public PageResult<QuestionResponse> queryQuestions(QueryQuestionRequest request) {
        log.info("分页查询题目，请求参数：{}", request);

        // 构建分页对象
        Page<Question> page = new Page<>(request.getPage(), request.getPageSize());

        // 查询
        IPage<Question> questionPage = baseMapper.selectQuestionPage(page, request);

        // 转换为响应对象
        List<QuestionResponse> responses = questionPage.getRecords().stream()
                .map(question -> {
                    QuestionResponse response = new QuestionResponse();
                    BeanUtils.copyProperties(question, response);
                    return response;
                })
                .collect(Collectors.toList());

        return new PageResult<>(responses, request.getPage(), request.getPageSize(), questionPage.getTotal());
    }

    @Override
    public List<Question> selectRandomQuestions(Long courseId,
                                                List<Long> chapterIds,
                                                String questionType,
                                                Integer difficulty,
                                                List<String> knowledgePoints,
                                                List<Long> excludeIds,
                                                Integer limit) {
        if (limit == null || limit <= 0) {
            return new ArrayList<>();
        }
        log.info("随机选择题目，课程ID：{}，章节ID：{}，题型：{}，难度：{}，知识点：{}，排除ID：{}，数量：{}",
                courseId, chapterIds, questionType, difficulty, knowledgePoints, excludeIds, limit);

        return baseMapper.selectRandomQuestions(
                courseId, chapterIds, questionType, difficulty, knowledgePoints, excludeIds, limit
        );
    }

    @Override
    public List<Map<String, Object>> countByQuestionType(Long courseId, List<Long> chapterIds) {
        log.info("统计题目数量（按题型），课程ID：{}，章节ID：{}", courseId, chapterIds);
        return baseMapper.countByQuestionType(courseId, chapterIds);
    }

    @Override
    public List<Map<String, Object>> countByDifficulty(Long courseId, List<Long> chapterIds) {
        log.info("统计题目数量（按难度），课程ID：{}，章节ID：{}", courseId, chapterIds);
        return baseMapper.countByDifficulty(courseId, chapterIds);
    }

    @Override
    public List<Map<String, Object>> countByKnowledgePoint(Long courseId, List<Long> chapterIds) {
        log.info("统计题目数量（按知识点），课程ID：{}，章节ID：{}", courseId, chapterIds);
        return baseMapper.countByKnowledgePoint(courseId, chapterIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer importQuestions(List<CreateQuestionRequest> questions, Long userId) {
        log.info("批量导入题目，数量：{}，创建人：{}", questions.size(), userId);

        int successCount = 0;
        for (CreateQuestionRequest request : questions) {
            try {
                createQuestion(request, userId);
                successCount++;
            } catch (Exception e) {
                log.error("导入题目失败，题目：{}，错误：{}", request.getTitle(), e.getMessage());
            }
        }

        log.info("批量导入题目完成，成功数量：{}", successCount);
        return successCount;
    }
}
