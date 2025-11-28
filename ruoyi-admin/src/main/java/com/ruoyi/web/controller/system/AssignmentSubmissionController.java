package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.AssignmentSubmission;
import com.ruoyi.system.service.IAssignmentSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 作业提交记录Controller
 */
@RestController
@RequestMapping("/system/assignment/submission")
public class AssignmentSubmissionController extends BaseController {

    @Autowired
    private IAssignmentSubmissionService assignmentSubmissionService;

    /**
     * 查询作业提交记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:query')")
    @GetMapping("/list")
    public TableDataInfo list(AssignmentSubmission assignmentSubmission) {
        if (assignmentSubmission.getAssignmentId() == null) {
            return getDataTable(Collections.emptyList());
        }
        assignmentSubmission.setIsDeleted(0);
        startPage();
        List<AssignmentSubmission> list = assignmentSubmissionService.selectAssignmentSubmissionList(assignmentSubmission);
        return getDataTable(list);
    }

    /**
     * 获取作业提交记录详情
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        AssignmentSubmission submission = assignmentSubmissionService.selectAssignmentSubmissionById(id);
        if (submission == null) {
            return error("提交记录不存在或已删除");
        }
        return success(submission);
    }

    /**
     * 批改作业提交记录
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:edit')")
    @Log(title = "作业提交记录", businessType = BusinessType.UPDATE)
    @PutMapping("/grade")
    public AjaxResult grade(@RequestBody AssignmentSubmission assignmentSubmission) {
        if (assignmentSubmission.getId() == null) {
            return error("提交记录ID不能为空");
        }
        int rows = assignmentSubmissionService.gradeAssignmentSubmission(assignmentSubmission);
        if (rows == 0) {
            return error("提交记录不存在或已删除");
        }
        return toAjax(rows);
    }
}
