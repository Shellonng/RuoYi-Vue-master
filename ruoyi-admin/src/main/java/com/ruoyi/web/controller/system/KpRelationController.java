package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.KpRelation;
import com.ruoyi.system.service.IKpRelationService;

/**
 * 知识点关系Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/kpRelation")
public class KpRelationController extends BaseController
{
    @Autowired
    private IKpRelationService kpRelationService;

    /**
     * 查询知识点关系列表
     */
    @GetMapping("/list")
    public AjaxResult list(KpRelation kpRelation)
    {
        List<KpRelation> list = kpRelationService.selectKpRelationList(kpRelation);
        return AjaxResult.success(list);
    }

    /**
     * 根据课程ID查询所有知识点关系
     */
    @GetMapping("/listByCourse/{courseId}")
    public AjaxResult listByCourse(@PathVariable Long courseId)
    {
        List<KpRelation> list = kpRelationService.selectKpRelationListByCourseId(courseId);
        return AjaxResult.success(list);
    }

    /**
     * 新增知识点关系
     */
    @Log(title = "知识点关系", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody KpRelation kpRelation)
    {
        kpRelation.setAiGenerated(0);  // 手动添加的关系
        return toAjax(kpRelationService.insertKpRelation(kpRelation));
    }

    /**
     * 修改知识点关系
     */
    @Log(title = "知识点关系", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody KpRelation kpRelation)
    {
        return toAjax(kpRelationService.updateKpRelation(kpRelation));
    }

    /**
     * 批量新增知识点关系
     */
    @Log(title = "批量新增知识点关系", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batchAdd(@RequestBody List<KpRelation> kpRelations)
    {
        return toAjax(kpRelationService.batchInsertKpRelations(kpRelations));
    }

    /**
     * 删除知识点关系
     */
    @Log(title = "知识点关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        return toAjax(kpRelationService.deleteKpRelationById(id));
    }

    /**
     * 根据课程ID删除所有知识点关系
     */
    @Log(title = "删除课程所有知识点关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/byCourse/{courseId}")
    public AjaxResult removeByCourse(@PathVariable Long courseId)
    {
        return toAjax(kpRelationService.deleteKpRelationsByCourseId(courseId));
    }

    /**
     * AI生成课程知识点关系图谱
     */
    @Log(title = "AI生成知识点关系图谱", businessType = BusinessType.INSERT)
    @PostMapping("/generateGraph/{courseId}")
    public AjaxResult generateGraph(@PathVariable Long courseId)
    {
        try
        {
            String result = kpRelationService.generateKnowledgeGraph(courseId);
            return AjaxResult.success(result);
        }
        catch (Exception e)
        {
            logger.error("生成知识点关系图谱失败", e);
            return AjaxResult.error("生成失败：" + e.getMessage());
        }
    }
}
