package com.exampl.smartcourse.service.smartpaper.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exampl.smartcourse.common.smartpaper.PageResult;
import com.exampl.smartcourse.dto.smartpaper.request.CreateAssignmentRequest;
import com.exampl.smartcourse.dto.smartpaper.request.PublishAssignmentRequest;
import com.exampl.smartcourse.dto.smartpaper.request.UpdateAssignmentRequest;
import com.exampl.smartcourse.dto.smartpaper.response.AssignmentDetailResponse;
import com.exampl.smartcourse.dto.smartpaper.response.AssignmentResponse;
import com.exampl.smartcourse.dto.smartpaper.response.QuestionDetailResponse;
import com.exampl.smartcourse.entity.smartpaper.Assignment;
import com.exampl.smartcourse.entity.smartpaper.AssignmentQuestion;
import com.exampl.smartcourse.mapper.smartpaper.AssignmentMapper;
import com.exampl.smartcourse.mapper.smartpaper.AssignmentQuestionMapper;
import com.exampl.smartcourse.service.smartpaper.IAssignmentService;
import com.exampl.smartcourse.service.smartpaper.IQuestionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 作业/考试服务实现类
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements IAssignmentService {

    private final AssignmentQuestionMapper assignmentQuestionMapper;
    private final IQuestionService questionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAssignment(CreateAssignmentRequest request, Long userId) {
        log.info("手动创建作业，标题：{}，课程ID：{}，题目数：{}", request.getTitle(), request.getCourseId(),
                CollectionUtils.isEmpty(request.getQuestionIds()) ? 0 : request.getQuestionIds().size());

        // 创建作业
        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setCourseId(request.getCourseId());
        assignment.setType(request.getType());
        assignment.setDescription(request.getDescription());
        assignment.setPublisherUserId(userId);
        assignment.setStatus(0);
        assignment.setMode("question");
        assignment.setTotalScore(request.getTotalScore() != null ? request.getTotalScore() : 100);
        // 仅考试类型设置时限
        if ("exam".equals(request.getType())) {
            assignment.setTimeLimit(request.getTimeLimit());
        }
        assignment.setStartTime(request.getStartTime());
        assignment.setEndTime(request.getEndTime());
        assignment.setAllowedFileTypes(request.getAllowedFileTypes());
        assignment.setAttachments(request.getAttachments());

        boolean saved = this.save(assignment);
        if (!saved) {
            throw new RuntimeException("创建作业失败");
        }

        // 添加题目
        if (!CollectionUtils.isEmpty(request.getQuestionIds())) {
            addQuestionsToAssignment(assignment.getId(), request.getQuestionIds());
        }

        return assignment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAssignment(Long assignmentId, UpdateAssignmentRequest request) {
        log.info("更新作业，作业ID：{}", assignmentId);

        Assignment assignment = this.getById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("作业不存在");
        }

        // 更新基本信息
        if (request.getTitle() != null) {
            assignment.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            assignment.setDescription(request.getDescription());
        }
        if (request.getTotalScore() != null) {
            assignment.setTotalScore(request.getTotalScore());
        }
        if (request.getTimeLimit() != null) {
            assignment.setTimeLimit(request.getTimeLimit());
        }
        if (request.getDuration() != null) {
            assignment.setDuration(request.getDuration());
        }
        if (request.getStartTime() != null) {
            assignment.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            assignment.setEndTime(request.getEndTime());
        }
        if (request.getAllowedFileTypes() != null) {
            assignment.setAllowedFileTypes(request.getAllowedFileTypes());
        }
        if (request.getAttachments() != null) {
            assignment.setAttachments(request.getAttachments());
        }
        boolean updated = this.updateById(assignment);

        // 更新题目（如果提供）
        if (request.getQuestionIds() != null) {
            assignmentQuestionMapper.deleteByAssignmentId(assignmentId);
            if (!CollectionUtils.isEmpty(request.getQuestionIds())) {
                addQuestionsToAssignment(assignmentId, request.getQuestionIds());
            }
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishAssignment(PublishAssignmentRequest request) {
        log.info("发布作业，请求参数：{}", request);

        Assignment assignment = this.getById(request.getAssignmentId());
        if (assignment == null) {
            throw new RuntimeException("作业不存在");
        }

        // 更新发布信息
        assignment.setStatus(1);
        assignment.setStartTime(request.getStartTime());
        assignment.setEndTime(request.getEndTime());
        assignment.setMode(request.getMode());
        assignment.setDuration(request.getDuration());
        assignment.setAllowedFileTypes(request.getAllowedFileTypes());
        assignment.setAttachments(request.getAttachments());

        return this.updateById(assignment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unpublishAssignment(Long assignmentId) {
        log.info("取消发布作业，作业ID：{}", assignmentId);

        Assignment assignment = this.getById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("作业不存在");
        }

        assignment.setStatus(0);
        return this.updateById(assignment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAssignment(Long assignmentId) {
        log.info("删除作业，作业ID：{}", assignmentId);

        // 删除作业
        boolean deleted = this.removeById(assignmentId);

        // 删除作业题目关联
        assignmentQuestionMapper.deleteByAssignmentId(assignmentId);

        return deleted;
    }

    @Override
    public AssignmentDetailResponse getAssignmentDetail(Long assignmentId) {
        log.info("查询作业详情，作业ID：{}", assignmentId);

        Assignment assignment = this.getById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("作业不存在");
        }

        AssignmentDetailResponse response = new AssignmentDetailResponse();
        BeanUtils.copyProperties(assignment, response);

        // 查询题目列表
        List<Long> questionIds = assignmentQuestionMapper.selectQuestionIdsByAssignmentId(assignmentId);
        if (!CollectionUtils.isEmpty(questionIds)) {
            List<QuestionDetailResponse> questions = questionIds.stream()
                    .map(questionService::getQuestionDetail)
                    .collect(Collectors.toList());
            response.setQuestions(questions);
            response.setQuestionCount(questions.size());
        }

        return response;
    }

    @Override
    public PageResult<AssignmentResponse> queryAssignments(Long courseId, String type, Integer status,
            Long publisherUserId, Integer page, Integer pageSize) {
        log.info("查询作业列表，课程ID：{}，类型：{}，状态：{}", courseId, type, status);

        Page<Assignment> pageObj = new Page<>(page, pageSize);
        IPage<Assignment> assignmentPage = baseMapper.selectAssignmentPage(pageObj, courseId, type, status,
                publisherUserId);

        List<AssignmentResponse> responses = assignmentPage.getRecords().stream()
                .map(assignment -> {
                    AssignmentResponse response = new AssignmentResponse();
                    BeanUtils.copyProperties(assignment, response);
                    // 查询题目数量
                    Integer count = assignmentQuestionMapper.countByAssignmentId(assignment.getId());
                    response.setQuestionCount(count != null ? count : 0);
                    return response;
                })
                .collect(Collectors.toList());

        return new PageResult<>(responses, page, pageSize, assignmentPage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addQuestionsToAssignment(Long assignmentId, List<Long> questionIds) {
        log.info("添加题目到作业，作业ID：{}，题目数：{}", assignmentId, questionIds.size());

        if (CollectionUtils.isEmpty(questionIds)) {
            return true;
        }

        // 查询当前最大序号
        LambdaQueryWrapper<AssignmentQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentQuestion::getAssignmentId, assignmentId)
                .orderByDesc(AssignmentQuestion::getSequence)
                .last("LIMIT 1");
        AssignmentQuestion lastAq = assignmentQuestionMapper.selectOne(wrapper);
        int startSequence = lastAq != null ? lastAq.getSequence() + 1 : 1;

        // 默认每题5分
        int scorePerQuestion = 5;

        List<AssignmentQuestion> assignmentQuestions = new ArrayList<>();
        for (int i = 0; i < questionIds.size(); i++) {
            AssignmentQuestion aq = new AssignmentQuestion();
            aq.setAssignmentId(assignmentId);
            aq.setQuestionId(questionIds.get(i));
            aq.setScore(scorePerQuestion);
            aq.setSequence(startSequence + i);
            assignmentQuestions.add(aq);
        }

        assignmentQuestionMapper.batchInsert(assignmentQuestions);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeQuestionsFromAssignment(Long assignmentId, List<Long> questionIds) {
        log.info("从作业中移除题目，作业ID：{}，题目数：{}", assignmentId, questionIds.size());

        if (CollectionUtils.isEmpty(questionIds)) {
            return true;
        }

        LambdaQueryWrapper<AssignmentQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentQuestion::getAssignmentId, assignmentId)
                .in(AssignmentQuestion::getQuestionId, questionIds);

        assignmentQuestionMapper.delete(wrapper);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long smartGenerate(com.exampl.smartcourse.dto.smartpaper.request.SmartPaperRequest request, Long userId) {
        log.info("智能组卷，标题：{}，课程ID：{}", request.getTitle(), request.getCourseId());

        // 1. 随机选择题目
        List<String> kpList = null;
        if (!CollectionUtils.isEmpty(request.getKnowledgePointIds())) {
            kpList = request.getKnowledgePointIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        }

        List<com.exampl.smartcourse.entity.smartpaper.Question> questions = questionService.selectRandomQuestions(
                request.getCourseId(),
                null, // chapterIds
                null, // questionType
                request.getDifficulty(),
                kpList,
                null, // excludeIds
                request.getQuestionCount());

        if (CollectionUtils.isEmpty(questions)) {
            throw new RuntimeException("未找到符合条件的题目，请调整条件后重试");
        }

        // 2. 创建作业
        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setCourseId(request.getCourseId());
        assignment.setType(request.getType());
        assignment.setDescription("智能组卷生成");
        assignment.setPublisherUserId(userId);
        assignment.setStatus(0);
        assignment.setMode("question");
        assignment.setTotalScore(questions.size() * 5); // 默认每题5分
        assignment.setTimeLimit(60); // 默认60分钟

        boolean saved = this.save(assignment);
        if (!saved) {
            throw new RuntimeException("创建作业失败");
        }

        // 3. 关联题目
        List<Long> questionIds = questions.stream()
                .map(com.exampl.smartcourse.entity.smartpaper.Question::getId)
                .collect(Collectors.toList());

        addQuestionsToAssignment(assignment.getId(), questionIds);

        return assignment.getId();
    }
}
