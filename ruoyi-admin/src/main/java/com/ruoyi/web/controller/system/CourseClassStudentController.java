package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.CourseEnrollmentRequest;
import com.ruoyi.system.service.ICourseEnrollmentRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级学生管理Controller
 */
@RestController
@RequestMapping("/system/class/student")
public class CourseClassStudentController extends BaseController {
    
    @Autowired
    private ICourseEnrollmentRequestService enrollmentService;

    /**
     * 查询班级学生列表（可根据状态过滤）
     */
    @PreAuthorize("@ss.hasPermi('system:class:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Long courseId, 
                              @RequestParam(required = false) Integer status) {
        startPage();
        // 查询该课程的选课申请
        CourseEnrollmentRequest query = new CourseEnrollmentRequest();
        query.setCourseId(courseId);
        if (status != null) {
            query.setStatus(status); // 根据参数过滤状态
        }
        
        List<CourseEnrollmentRequest> list = enrollmentService.selectEnrollmentList(query);
        return getDataTable(list);
    }

    /**
     * 移除学生（删除选课记录）
     */
    @PreAuthorize("@ss.hasPermi('system:class:remove')")
    @Log(title = "班级学生管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        // 删除选课申请记录
        int result = 0;
        for (Long id : ids) {
            result += enrollmentService.deleteEnrollmentById(id);
        }
        return toAjax(result);
    }
}
