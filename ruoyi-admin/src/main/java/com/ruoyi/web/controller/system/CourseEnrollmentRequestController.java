package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.CourseEnrollmentRequest;
import com.ruoyi.system.service.ICourseEnrollmentRequestService;
import com.ruoyi.web.controller.system.dto.BatchReviewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * 选课申请Controller
 */
@RestController
@RequestMapping("/system/enrollment")
public class CourseEnrollmentRequestController extends BaseController {
    @Autowired
    private ICourseEnrollmentRequestService enrollmentService;

    /**
     * 查询选课申请列表（教师查看自己所教课程的选课申请）
     */
    @PreAuthorize("@ss.hasPermi('system:enrollment:list')")
    @GetMapping("/list")
    public TableDataInfo list(CourseEnrollmentRequest enrollment) {
        startPage();
        List<CourseEnrollmentRequest> list = enrollmentService.selectEnrollmentListByTeacher();
        return getDataTable(list);
    }

    /**
     * 获取选课申请详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:enrollment:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(enrollmentService.selectEnrollmentById(id));
    }

    /**
     * 新增选课申请
     */
    @PreAuthorize("@ss.hasPermi('system:enrollment:add')")
    @Log(title = "选课申请", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CourseEnrollmentRequest enrollment) {
        return toAjax(enrollmentService.insertEnrollment(enrollment));
    }

    /**
     * 修改选课申请
     */
    @PreAuthorize("@ss.hasPermi('system:enrollment:edit')")
    @Log(title = "选课申请", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CourseEnrollmentRequest enrollment) {
        enrollment.setReviewTime(new Date());
        return toAjax(enrollmentService.updateEnrollment(enrollment));
    }

    /**
     * 删除选课申请
     */
    @PreAuthorize("@ss.hasPermi('system:enrollment:remove')")
    @Log(title = "选课申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(enrollmentService.deleteEnrollmentByIds(ids));
    }

    /**
     * 批量审核通过
     */
    @PreAuthorize("@ss.hasPermi('system:enrollment:edit')")
    @Log(title = "批量审核通过", businessType = BusinessType.UPDATE)
    @PostMapping("/batchApprove")
    public AjaxResult batchApprove(@RequestBody BatchReviewDTO dto) {
        List<Long> idList = dto.getIds();
        if (idList == null || idList.isEmpty()) {
            return error("请选择要审核的记录");
        }
        Long[] ids = idList.toArray(new Long[0]);
        String reviewComment = dto.getReviewComment();
        if (reviewComment == null || reviewComment.isEmpty()) {
            reviewComment = "同意选课申请";
        }
        return toAjax(enrollmentService.batchApprove(ids, reviewComment));
    }

    /**
     * 批量拒绝
     */
    @PreAuthorize("@ss.hasPermi('system:enrollment:edit')")
    @Log(title = "批量拒绝", businessType = BusinessType.UPDATE)
    @PostMapping("/batchReject")
    public AjaxResult batchReject(@RequestBody BatchReviewDTO dto) {
        List<Long> idList = dto.getIds();
        if (idList == null || idList.isEmpty()) {
            return error("请选择要拒绝的记录");
        }
        Long[] ids = idList.toArray(new Long[0]);
        String reviewComment = dto.getReviewComment();
        if (reviewComment == null || reviewComment.isEmpty()) {
            reviewComment = "拒绝选课申请";
        }
        return toAjax(enrollmentService.batchReject(ids, reviewComment));
    }

    /**
     * 导出选课申请列表
     */
    @PreAuthorize("@ss.hasPermi('system:enrollment:export')")
    @Log(title = "选课申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CourseEnrollmentRequest enrollment) {
        List<CourseEnrollmentRequest> list = enrollmentService.selectEnrollmentListByTeacher();
        ExcelUtil<CourseEnrollmentRequest> util = new ExcelUtil<CourseEnrollmentRequest>(CourseEnrollmentRequest.class);
        util.exportExcel(response, list, "选课申请数据");
    }
}
