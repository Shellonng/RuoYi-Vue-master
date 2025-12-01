package com.exampl.smartcourse.controller.smartpaper;

import com.exampl.smartcourse.common.smartpaper.PageResult;
import com.exampl.smartcourse.common.smartpaper.Result;
import com.exampl.smartcourse.dto.smartpaper.request.CreateQuestionRequest;
import com.exampl.smartcourse.dto.smartpaper.request.QueryQuestionRequest;
import com.exampl.smartcourse.dto.smartpaper.request.UpdateQuestionRequest;
import com.exampl.smartcourse.dto.smartpaper.response.QuestionDetailResponse;
import com.exampl.smartcourse.dto.smartpaper.response.QuestionResponse;
import com.exampl.smartcourse.service.smartpaper.IQuestionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 题目管理Controller
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Slf4j
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@CrossOrigin(
    origins = {"http://localhost", "http://localhost:80", "http://localhost:81", "http://localhost:5173"}, 
    allowCredentials = "true",
    allowedHeaders = {"Content-Type", "Authorization", "userId", "userid", "X-Requested-With", "*"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
    maxAge = 3600
)
@Tag(name = "题目管理", description = "题目的增删改查、统计分析等接口")
public class QuestionController {

    private final IQuestionService questionService;

    @PostMapping
    @Operation(summary = "创建题目", description = "创建新题目（包含选项）")
    public Result<Long> createQuestion(
            @Validated @RequestBody CreateQuestionRequest request,
            @Parameter(description = "创建人ID，实际应从token中获取") @RequestHeader(value = "userId", defaultValue = "20001") Long userId) {
        log.info("创建题目接口被调用，请求参数：{}", request);
        Long questionId = questionService.createQuestion(request, userId);
        return Result.success("创建题目成功", questionId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新题目", description = "更新题目信息")
    public Result<Boolean> updateQuestion(
            @Parameter(description = "题目ID") @PathVariable Long id,
            @Validated @RequestBody UpdateQuestionRequest request) {
        log.info("更新题目接口被调用，题目ID：{}，请求参数：{}", id, request);
        request.setId(id);
        Boolean result = questionService.updateQuestion(request);
        return Result.success("更新题目成功", result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除题目", description = "逻辑删除题目")
    public Result<Boolean> deleteQuestion(
            @Parameter(description = "题目ID") @PathVariable Long id) {
        log.info("删除题目接口被调用，题目ID：{}", id);
        Boolean result = questionService.deleteQuestion(id);
        return Result.success("删除题目成功", result);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除题目", description = "批量逻辑删除题目")
    public Result<Boolean> batchDeleteQuestions(
            @Parameter(description = "题目ID列表") @RequestBody List<Long> questionIds) {
        log.info("批量删除题目接口被调用，题目ID列表：{}", questionIds);
        Boolean result = questionService.batchDeleteQuestions(questionIds);
        return Result.success("批量删除题目成功", result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询题目详情", description = "根据ID查询题目详细信息（包含选项）")
    public Result<QuestionDetailResponse> getQuestionDetail(
            @Parameter(description = "题目ID") @PathVariable Long id) {
        log.info("查询题目详情接口被调用，题目ID：{}", id);
        QuestionDetailResponse response = questionService.getQuestionDetail(id);
        return Result.success(response);
    }

    @PostMapping("/query")
    @Operation(summary = "分页查询题目", description = "根据条件分页查询题目列表")
    public Result<PageResult<QuestionResponse>> queryQuestions(
            @Validated @RequestBody QueryQuestionRequest request) {
        log.info("分页查询题目接口被调用，请求参数：{}", request);
        PageResult<QuestionResponse> result = questionService.queryQuestions(request);
        return Result.success(result);
    }

    @GetMapping
    @Operation(summary = "分页查询题目（GET）", description = "通过查询参数分页获取题目列表")
    public Result<PageResult<QuestionResponse>> listQuestions(
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "章节ID") @RequestParam(required = false) Long chapterId,
            @Parameter(description = "题型") @RequestParam(required = false) String questionType,
            @Parameter(description = "难度等级(1-5)") @RequestParam(required = false) Integer difficulty,
            @Parameter(description = "知识点（模糊查询）") @RequestParam(required = false) String knowledgePoint,
            @Parameter(description = "题干关键字") @RequestParam(required = false) String title,
            @Parameter(description = "创建人ID") @RequestParam(required = false) Long createdBy,
            @Parameter(description = "排序字段，支持create_time/difficulty") @RequestParam(defaultValue = "create_time") String sortField,
            @Parameter(description = "排序方式：asc/desc") @RequestParam(defaultValue = "desc") String sortOrder,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {

        QueryQuestionRequest request = new QueryQuestionRequest();
        request.setCourseId(courseId);
        request.setChapterId(chapterId);
        request.setQuestionType(questionType);
        request.setDifficulty(difficulty);
        request.setKnowledgePoint(knowledgePoint);
        request.setTitle(title);
        request.setCreatedBy(createdBy);
        request.setSortField(sortField);
        request.setSortOrder(sortOrder);
        request.setPage(page);
        request.setPageSize(pageSize);

        log.info("GET方式分页查询题目接口被调用，请求参数：{}", request);
        PageResult<QuestionResponse> result = questionService.queryQuestions(request);
        return Result.success(result);
    }

    @GetMapping("/statistics/question-type")
    @Operation(summary = "统计题目数量（按题型）", description = "统计指定课程/章节的题目数量分布（按题型）")
    public Result<List<Map<String, Object>>> countByQuestionType(
            @Parameter(description = "课程ID") @RequestParam Long courseId,
            @Parameter(description = "章节ID列表") @RequestParam(required = false) List<Long> chapterIds) {
        log.info("统计题目数量（按题型）接口被调用，课程ID：{}，章节ID：{}", courseId, chapterIds);
        List<Map<String, Object>> result = questionService.countByQuestionType(courseId, chapterIds);
        return Result.success(result);
    }

    @GetMapping("/statistics/difficulty")
    @Operation(summary = "统计题目数量（按难度）", description = "统计指定课程/章节的题目数量分布（按难度）")
    public Result<List<Map<String, Object>>> countByDifficulty(
            @Parameter(description = "课程ID") @RequestParam Long courseId,
            @Parameter(description = "章节ID列表") @RequestParam(required = false) List<Long> chapterIds) {
        log.info("统计题目数量（按难度）接口被调用，课程ID：{}，章节ID：{}", courseId, chapterIds);
        List<Map<String, Object>> result = questionService.countByDifficulty(courseId, chapterIds);
        return Result.success(result);
    }

    @GetMapping("/statistics/knowledge-point")
    @Operation(summary = "统计题目数量（按知识点）", description = "统计指定课程/章节的题目数量分布（按知识点）")
    public Result<List<Map<String, Object>>> countByKnowledgePoint(
            @Parameter(description = "课程ID") @RequestParam Long courseId,
            @Parameter(description = "章节ID列表") @RequestParam(required = false) List<Long> chapterIds) {
        log.info("统计题目数量（按知识点）接口被调用，课程ID：{}，章节ID：{}", courseId, chapterIds);
        List<Map<String, Object>> result = questionService.countByKnowledgePoint(courseId, chapterIds);
        return Result.success(result);
    }

    @PostMapping("/import")
    @Operation(summary = "批量导入题目", description = "批量导入题目")
    public Result<Integer> importQuestions(
            @RequestBody List<CreateQuestionRequest> questions,
            @Parameter(description = "创建人ID，实际应从token中获取") @RequestHeader(value = "userId", defaultValue = "20001") Long userId) {
        log.info("批量导入题目接口被调用，题目数量：{}", questions.size());
        Integer count = questionService.importQuestions(questions, userId);
        return Result.success("成功导入" + count + "道题目", count);
    }
}
