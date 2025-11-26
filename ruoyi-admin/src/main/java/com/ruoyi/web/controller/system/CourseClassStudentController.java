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
     * 移除学生(删除选课记录)
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

    /**
     * 移除学生(将状态置为2-拒绝)
     */
    @PreAuthorize("@ss.hasPermi('system:class:remove')")
    @Log(title = "班级学生管理", businessType = BusinessType.UPDATE)
    @PutMapping("/remove")
    public AjaxResult removeByUpdateStatus(@RequestBody CourseEnrollmentRequest request) {
        // 将选中的学生状态更新为2(已拒绝)
        Long[] ids = request.getIds();
        if (ids == null || ids.length == 0) {
            return error("请选择要移除的学生");
        }
        
        int result = 0;
        for (Long id : ids) {
            CourseEnrollmentRequest enrollment = new CourseEnrollmentRequest();
            enrollment.setId(id);
            enrollment.setStatus(2); // 状态改为拒绝
            enrollment.setReviewComment("已从班级移除");
            enrollment.setReviewTime(new java.util.Date());
            result += enrollmentService.updateEnrollment(enrollment);
        }
        return toAjax(result);
    }
}
