package com.ruoyi.web.controller.system;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.Course;
import com.ruoyi.system.domain.vo.KnowledgePointErrorStats;
import com.ruoyi.system.service.ICourseService;
import com.ruoyi.system.service.IKnowledgePointService;
import com.ruoyi.system.service.ITeachingPlanService;

/**
 * 课程 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/course")
public class CourseController extends BaseController
{
    @Autowired
    private ICourseService courseService;

    @Autowired
    private IKnowledgePointService knowledgePointService;
    
    @Autowired
    private ITeachingPlanService teachingPlanService;

    /**
     * 获取课程列表（当前教师的课程）
     */
    @GetMapping("/list")
    public TableDataInfo list(Course course)
    {
        startPage();
        List<Course> list = courseService.selectMyCourseList();
        return getDataTable(list);
    }

    /**
     * 获取课程统计信息
     */
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        Map<String, Object> stats = courseService.selectCourseStats();
        return success(stats);
    }

    /**
     * 根据课程编号获取详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(courseService.selectCourseById(id));
    }

    /**
     * 新增课程
     */
    @Log(title = "课程", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Course course)
    {
        return toAjax(courseService.insertCourse(course));
    }

    /**
     * 修改课程
     */
    @Log(title = "课程", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Course course)
    {
        return toAjax(courseService.updateCourse(course));
    }

    /**
     * 删除课程
     */
    @Log(title = "课程", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(courseService.deleteCourseByIds(ids));
    }

    /**
     * 获取知识点错误统计
     */
    @GetMapping("/kpErrorStats")
    public AjaxResult getKpErrorStats(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String targetDate)
    {
        List<KnowledgePointErrorStats> stats = knowledgePointService.selectKnowledgePointErrorStats(courseId, targetDate);
        return success(stats);
    }
    
    /**
     * AI生成教学计划
     */
    @Log(title = "生成教学计划", businessType = BusinessType.OTHER)
    @PostMapping("/generateTeachingPlan")
    public AjaxResult generateTeachingPlan(@RequestBody Map<String, Object> params)
    {
        try
        {
            Map<String, Object> planData = teachingPlanService.generateTeachingPlan(params);
            return success(planData);
        }
        catch (Exception e)
        {
            logger.error("生成教学计划失败", e);
            return error("生成教学计划失败: " + e.getMessage());
        }
    }
}
