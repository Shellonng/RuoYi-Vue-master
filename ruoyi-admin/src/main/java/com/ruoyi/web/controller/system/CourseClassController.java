package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.Course;
import com.ruoyi.system.service.ICourseService;
import com.ruoyi.system.utils.BusinessUserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 班级管理Controller (基于课程)
 */
@RestController
@RequestMapping("/system/class")
public class CourseClassController extends BaseController {
    
    @Autowired
    private ICourseService courseService;

    /**
     * 查询班级列表（教师查看自己所教课程作为班级）
     */
    @PreAuthorize("@ss.hasPermi('system:class:list')")
    @GetMapping("/list")
    public TableDataInfo list(Course course) {
        startPage();
        // 获取当前教师ID
        Long teacherUserId = BusinessUserUtils.getCurrentBusinessUserId();
        course.setTeacherUserId(teacherUserId);
        
        List<Course> list = courseService.selectCourseList(course);
        return getDataTable(list);
    }

    /**
     * 获取班级详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:class:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        Course course = courseService.selectCourseById(id);
        if (course == null) {
            return error("班级不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", course.getId());
        result.put("title", course.getTitle());
        result.put("studentCount", course.getStudentCount());
        result.put("description", course.getDescription());
        
        return success(result);
    }

    /**
     * 修改班级信息（实际修改课程）
     */
    @PreAuthorize("@ss.hasPermi('system:class:edit')")
    @Log(title = "班级管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Course course) {
        return toAjax(courseService.updateCourse(course));
    }
}
