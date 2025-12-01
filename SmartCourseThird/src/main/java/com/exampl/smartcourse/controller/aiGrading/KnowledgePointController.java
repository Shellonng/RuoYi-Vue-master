package com.exampl.smartcourse.controller.aiGrading;

import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.entity.aiGrading.KnowledgePoint;
import com.exampl.smartcourse.mapper.aiGrading.KnowledgePointMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识点管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge-points")
@RequiredArgsConstructor
@Tag(name = "知识点管理", description = "知识点的查询等接口")
public class KnowledgePointController {

    private final KnowledgePointMapper knowledgePointMapper;

    @GetMapping("/list")
    @Operation(summary = "查询课程知识点列表", description = "根据课程ID查询知识点列表")
    public Result<List<KnowledgePoint>> listByCourseId(
            @Parameter(description = "课程ID") @RequestParam Long courseId) {
        log.info("查询课程知识点列表，课程ID：{}", courseId);
        List<KnowledgePoint> list = knowledgePointMapper.selectByCourseId(courseId);
        return Result.success(list);
    }
}
