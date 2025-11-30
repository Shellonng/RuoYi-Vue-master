package com.ruoyi.web.controller.course;

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
import com.ruoyi.course.domain.KnowledgeGraph;
import com.ruoyi.course.service.IKnowledgeGraphService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.utils.BusinessUserUtils;

/**
 * 知识图谱Controller
 * 
 * @author ruoyi
 * @date 2025-11-30
 */
@RestController
@RequestMapping("/knowledgeGraph")
public class KnowledgeGraphController extends BaseController
{
    @Autowired
    private IKnowledgeGraphService knowledgeGraphService;

    /**
     * 查询知识图谱列表
     */
    @PreAuthorize("@ss.hasPermi('course:knowledgeGraph:list')")
    @GetMapping("/list")
    public TableDataInfo list(KnowledgeGraph knowledgeGraph)
    {
        startPage();
        List<KnowledgeGraph> list = knowledgeGraphService.selectKnowledgeGraphList(knowledgeGraph);
        return getDataTable(list);
    }

    /**
     * 导出知识图谱列表
     */
    @PreAuthorize("@ss.hasPermi('course:knowledgeGraph:export')")
    @Log(title = "知识图谱", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, KnowledgeGraph knowledgeGraph)
    {
        List<KnowledgeGraph> list = knowledgeGraphService.selectKnowledgeGraphList(knowledgeGraph);
        ExcelUtil<KnowledgeGraph> util = new ExcelUtil<KnowledgeGraph>(KnowledgeGraph.class);
        util.exportExcel(response, list, "知识图谱数据");
    }

    /**
     * 获取知识图谱详细信息
     */
    @PreAuthorize("@ss.hasPermi('course:knowledgeGraph:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(knowledgeGraphService.selectKnowledgeGraphById(id));
    }

    /**
     * 根据课程ID获取知识图谱
     */
    @GetMapping("/byCourse/{courseId}")
    public AjaxResult getByCourseId(@PathVariable("courseId") Long courseId, String graphType)
    {
        if (graphType == null || graphType.isEmpty()) {
            graphType = "COURSE"; // 默认为课程图谱
        }
        return success(knowledgeGraphService.selectKnowledgeGraphByCourseId(courseId, graphType));
    }

    /**
     * 新增知识图谱
     */
    @PreAuthorize("@ss.hasPermi('course:knowledgeGraph:add')")
    @Log(title = "知识图谱", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody KnowledgeGraph knowledgeGraph)
    {
        return toAjax(knowledgeGraphService.insertKnowledgeGraph(knowledgeGraph));
    }

    /**
     * 修改知识图谱
     */
    @PreAuthorize("@ss.hasPermi('course:knowledgeGraph:edit')")
    @Log(title = "知识图谱", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody KnowledgeGraph knowledgeGraph)
    {
        return toAjax(knowledgeGraphService.updateKnowledgeGraph(knowledgeGraph));
    }

    /**
     * 保存或更新知识图谱（智能判断）
     */
    @Log(title = "保存知识图谱", businessType = BusinessType.UPDATE)
    @PostMapping("/save")
    public AjaxResult save(@RequestBody KnowledgeGraph knowledgeGraph)
    {
        // 设置默认值
        if (knowledgeGraph.getGraphType() == null || knowledgeGraph.getGraphType().isEmpty()) {
            knowledgeGraph.setGraphType("COURSE");
        }
        if (knowledgeGraph.getStatus() == null || knowledgeGraph.getStatus().isEmpty()) {
            knowledgeGraph.setStatus("active");
        }
        if (knowledgeGraph.getIsDeleted() == null) {
            knowledgeGraph.setIsDeleted(0);
        }
        
        // 使用BusinessUserUtils获取当前业务用户ID（user表的id，而非sys_user表的id）
        Long businessUserId = BusinessUserUtils.getCurrentBusinessUserId();
        if (businessUserId == null) {
            return error("无法获取当前用户信息");
        }
        knowledgeGraph.setCreatorId(businessUserId);
        
        return toAjax(knowledgeGraphService.saveOrUpdateKnowledgeGraph(knowledgeGraph));
    }

    /**
     * 删除知识图谱
     */
    @PreAuthorize("@ss.hasPermi('course:knowledgeGraph:remove')")
    @Log(title = "知识图谱", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(knowledgeGraphService.deleteKnowledgeGraphByIds(ids));
    }
}
