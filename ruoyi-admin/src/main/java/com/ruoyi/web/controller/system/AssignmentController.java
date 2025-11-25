package com.ruoyi.web.controller.system;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.Assignment;
import com.ruoyi.system.service.IAssignmentService;
import com.ruoyi.system.utils.BusinessUserUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 作业或考试Controller
 * 
 * @author DYH
 * @date 2025-11-18
 */
@RestController
@RequestMapping("/system/assignment")
public class AssignmentController extends BaseController
{
    @Autowired
    private IAssignmentService assignmentService;

    /**
     * 查询作业或考试列表
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:list')")
    @GetMapping("/list")
    public TableDataInfo list(Assignment assignment)
    {
        startPage();
        List<Assignment> list = assignmentService.selectAssignmentList(assignment);
        return getDataTable(list);
    }

    /**
     * 导出作业或考试列表
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:export')")
    @Log(title = "作业或考试", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Assignment assignment)
    {
        List<Assignment> list = assignmentService.selectAssignmentList(assignment);
        ExcelUtil<Assignment> util = new ExcelUtil<Assignment>(Assignment.class);
        util.exportExcel(response, list, "作业或考试数据");
    }

    /**
     * 获取作业或考试详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(assignmentService.selectAssignmentById(id));
    }

    /**
     * 新增作业或考试
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:add')")
    @Log(title = "作业或考试", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Assignment assignment)
    {
        // 从token获取当前业务用户ID
        Long userId = BusinessUserUtils.getCurrentBusinessUserId();
        assignment.setPublisherUserId(userId);
        
        return toAjax(assignmentService.insertAssignment(assignment));
    }

    /**
     * 修改作业或考试
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:edit')")
    @Log(title = "作业或考试", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Assignment assignment)
    {
        return toAjax(assignmentService.updateAssignment(assignment));
    }

    /**
     * 删除作业或考试
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:remove')")
    @Log(title = "作业或考试", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(assignmentService.deleteAssignmentByIds(ids));
    }
    
    /**
     * 根据知识点ID查询关联的作业/考试/测验列表
     */
    @PreAuthorize("@ss.hasPermi('system:assignment:query')")
    @GetMapping("/byKnowledgePoint/{kpId}")
    public AjaxResult getByKnowledgePoint(@PathVariable("kpId") Long kpId)
    {
        List<Assignment> list = assignmentService.selectAssignmentsByKnowledgePointId(kpId);
        return success(list);
    }
}
