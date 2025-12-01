package com.exampl.smartcourse.controller.smartpaper;

import com.exampl.smartcourse.common.smartpaper.PageResult;
import com.exampl.smartcourse.common.smartpaper.Result;
import com.exampl.smartcourse.dto.smartpaper.request.CreateAssignmentRequest;
import com.exampl.smartcourse.dto.smartpaper.request.PublishAssignmentRequest;
import com.exampl.smartcourse.dto.smartpaper.request.UpdateAssignmentRequest;
import com.exampl.smartcourse.dto.smartpaper.response.AssignmentDetailResponse;
import com.exampl.smartcourse.dto.smartpaper.response.AssignmentResponse;
import com.exampl.smartcourse.service.smartpaper.IAssignmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作业管理Controller
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Slf4j
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Tag(name = "作业管理", description = "作业/考试的创建、发布、管理等接口")
public class AssignmentController {

    private final IAssignmentService assignmentService;

    @PostMapping("/create")
    @Operation(summary = "手动创建作业", description = "手动指定题目创建作业")
    public Result<Long> createAssignment(
            @Validated @RequestBody CreateAssignmentRequest request,
            @Parameter(description = "创建人ID，实际应从token中获取") @RequestHeader(value = "userId", defaultValue = "20001") Long userId) {
        log.info("手动创建作业接口被调用，标题：{}，课程ID：{}", request.getTitle(), request.getCourseId());
        Long assignmentId = assignmentService.createAssignment(request, userId);
        return Result.success("创建作业成功", assignmentId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新作业", description = "更新作业基本信息和题目")
    public Result<Boolean> updateAssignment(
            @Parameter(description = "作业ID") @PathVariable Long id,
            @Validated @RequestBody UpdateAssignmentRequest request) {
        log.info("更新作业接口被调用，作业ID：{}", id);
        Boolean result = assignmentService.updateAssignment(id, request);
        return Result.success("更新作业成功", result);
    }

    @PostMapping("/publish")
    @Operation(summary = "发布作业", description = "发布作业/考试，设置开始结束时间")
    public Result<Boolean> publishAssignment(
            @Validated @RequestBody PublishAssignmentRequest request) {
        log.info("发布作业接口被调用，请求参数：{}", request);
        Boolean result = assignmentService.publishAssignment(request);
        return Result.success("发布作业成功", result);
    }

    @PostMapping("/{id}/unpublish")
    @Operation(summary = "取消发布作业", description = "取消发布作业/考试")
    public Result<Boolean> unpublishAssignment(
            @Parameter(description = "作业ID") @PathVariable Long id) {
        log.info("取消发布作业接口被调用，作业ID：{}", id);
        Boolean result = assignmentService.unpublishAssignment(id);
        return Result.success("取消发布成功", result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除作业", description = "逻辑删除作业")
    public Result<Boolean> deleteAssignment(
            @Parameter(description = "作业ID") @PathVariable Long id) {
        log.info("删除作业接口被调用，作业ID：{}", id);
        Boolean result = assignmentService.deleteAssignment(id);
        return Result.success("删除作业成功", result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询作业详情", description = "查询作业详细信息（包含题目列表）")
    public Result<AssignmentDetailResponse> getAssignmentDetail(
            @Parameter(description = "作业ID") @PathVariable Long id) {
        log.info("查询作业详情接口被调用，作业ID：{}", id);
        AssignmentDetailResponse response = assignmentService.getAssignmentDetail(id);
        return Result.success(response);
    }

    @GetMapping("/list")
    @Operation(summary = "查询作业列表（教师端）", description = "分页查询作业列表")
    public Result<PageResult<AssignmentResponse>> queryAssignments(
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "类型：homework/exam") @RequestParam(required = false) String type,
            @Parameter(description = "状态：0-未发布，1-已发布") @RequestParam(required = false) Integer status,
            @Parameter(description = "发布者ID") @RequestParam(required = false) Long publisherUserId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("查询作业列表接口被调用，课程ID：{}，类型：{}，状态：{}", courseId, type, status);
        PageResult<AssignmentResponse> result = assignmentService.queryAssignments(
                courseId, type, status, publisherUserId, page, pageSize);
        return Result.success(result);
    }

    @PostMapping("/{id}/questions/add")
    @Operation(summary = "添加题目到作业", description = "向作业中添加题目")
    public Result<Boolean> addQuestionsToAssignment(
            @Parameter(description = "作业ID") @PathVariable Long id,
            @Parameter(description = "题目ID列表") @RequestBody List<Long> questionIds) {
        int size = questionIds != null ? questionIds.size() : 0;
        log.info("添加题目到作业接口被调用，作业ID：{}，题目数：{}", id, size);
        Boolean result = assignmentService.addQuestionsToAssignment(id, questionIds);
        return Result.success("添加题目成功", result);
    }

    @PostMapping("/{id}/questions/remove")
    @Operation(summary = "从作业中移除题目", description = "从作业中移除指定题目")
    public Result<Boolean> removeQuestionsFromAssignment(
            @Parameter(description = "作业ID") @PathVariable Long id,
            @Parameter(description = "题目ID列表") @RequestBody List<Long> questionIds) {
        int size = questionIds != null ? questionIds.size() : 0;
        log.info("从作业中移除题目接口被调用，作业ID：{}，题目数：{}", id, size);
        Boolean result = assignmentService.removeQuestionsFromAssignment(id, questionIds);
        return Result.success("移除题目成功", result);
    }

    @PostMapping("/smart-generate")
    @Operation(summary = "智能组卷", description = "根据条件自动生成作业")
    public Result<Long> smartGenerate(
            @Validated @RequestBody com.exampl.smartcourse.dto.smartpaper.request.SmartPaperRequest request,
            @Parameter(description = "创建人ID") @RequestHeader(value = "userId", defaultValue = "20001") Long userId) {
        log.info("智能组卷接口被调用，标题：{}，课程ID：{}", request.getTitle(), request.getCourseId());
        Long assignmentId = assignmentService.smartGenerate(request, userId);
        return Result.success("智能组卷成功", assignmentId);
    }
}
