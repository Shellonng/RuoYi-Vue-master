package com.ruoyi.web.controller.system;

import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.AssignmentKp;
import com.ruoyi.system.service.IAssignmentKpService;

/**
 * 作业与知识点关联 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/assignmentKp")
public class AssignmentKpController extends BaseController
{
    @Autowired
    private IAssignmentKpService assignmentKpService;

    /**
     * 获取作业知识点关联列表
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssignmentKp assignmentKp)
    {
        startPage();
        List<AssignmentKp> list = assignmentKpService.selectAssignmentKpList(assignmentKp);
        return getDataTable(list);
    }

    /**
     * 根据作业ID获取关联的知识点列表（含知识点详情）
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:query')")
    @GetMapping("/list/{assignmentId}")
    public AjaxResult listByAssignment(@PathVariable Long assignmentId)
    {
        List<AssignmentKp> list = assignmentKpService.selectAssignmentKpListByAssignmentId(assignmentId);
        return success(list);
    }

    /**
     * 根据知识点ID获取关联的作业列表
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:list')")
    @GetMapping("/listByKp/{kpId}")
    public AjaxResult listByKp(@PathVariable Long kpId)
    {
        List<AssignmentKp> list = assignmentKpService.selectAssignmentKpListByKpId(kpId);
        return success(list);
    }

    /**
     * 根据关联ID获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(assignmentKpService.selectAssignmentKpById(id));
    }

    /**
     * 新增作业知识点关联
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:add')")
    @Log(title = "作业知识点关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AssignmentKp assignmentKp)
    {
        int result = assignmentKpService.insertAssignmentKp(assignmentKp);
        if (result > 0) {
            return success(assignmentKp);
        }
        return error("关联已存在或新增失败");
    }

    /**
     * 删除作业知识点关联
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:remove')")
    @Log(title = "作业知识点关联", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(assignmentKpService.deleteAssignmentKpByIds(ids));
    }

    /**
     * 删除作业的所有知识点关联
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:remove')")
    @Log(title = "删除作业的所有知识点", businessType = BusinessType.DELETE)
    @DeleteMapping("/assignment/{assignmentId}")
    public AjaxResult removeByAssignment(@PathVariable Long assignmentId)
    {
        return toAjax(assignmentKpService.deleteAssignmentKpByAssignmentId(assignmentId));
    }

    /**
     * 批量新增作业知识点关联
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:add')")
    @Log(title = "批量新增作业知识点关联", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batchInsert(@RequestBody List<AssignmentKp> assignmentKpList)
    {
        int result = assignmentKpService.batchInsertAssignmentKp(assignmentKpList);
        if (result > 0) {
            return success(assignmentKpList);
        }
        return error("批量创建关联失败");
    }

    /**
     * 为作业批量设置知识点（先删除旧关联，再创建新关联）
     * 
     * @param assignmentId 作业ID
     * @param kpIds 知识点ID数组
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:edit')")
    @Log(title = "设置作业知识点", businessType = BusinessType.UPDATE)
    @PostMapping("/set/{assignmentId}")
    public AjaxResult setKnowledgePoints(@PathVariable Long assignmentId, @RequestBody Long[] kpIds)
    {
        int result = assignmentKpService.setAssignmentKnowledgePoints(assignmentId, kpIds);
        return toAjax(result);
    }
}
