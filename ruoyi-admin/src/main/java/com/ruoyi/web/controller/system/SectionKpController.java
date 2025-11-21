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
import com.ruoyi.system.domain.SectionKp;
import com.ruoyi.system.service.ISectionKpService;

/**
 * 小节与知识点关联 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/sectionKp")
public class SectionKpController extends BaseController
{
    @Autowired
    private ISectionKpService sectionKpService;

    /**
     * 获取小节知识点关联列表
     */
    @GetMapping("/list")
    public TableDataInfo list(SectionKp sectionKp)
    {
        startPage();
        List<SectionKp> list = sectionKpService.selectSectionKpList(sectionKp);
        return getDataTable(list);
    }

    /**
     * 根据小节ID获取关联的知识点列表（含知识点详情）
     */
    @GetMapping("/listBySection/{sectionId}")
    public AjaxResult listBySection(@PathVariable Long sectionId)
    {
        List<SectionKp> list = sectionKpService.selectSectionKpListBySectionId(sectionId);
        return success(list);
    }

    /**
     * 根据知识点ID获取关联的小节列表
     */
    @GetMapping("/listByKp/{kpId}")
    public AjaxResult listByKp(@PathVariable Long kpId)
    {
        List<SectionKp> list = sectionKpService.selectSectionKpListByKpId(kpId);
        return success(list);
    }

    /**
     * 根据关联ID获取详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(sectionKpService.selectSectionKpById(id));
    }

    /**
     * 新增小节知识点关联
     */
    @Log(title = "小节知识点关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SectionKp sectionKp)
    {
        int result = sectionKpService.insertSectionKp(sectionKp);
        if (result > 0) {
            return success(sectionKp);
        }
        return error("关联已存在或新增失败");
    }

    /**
     * 删除小节知识点关联
     */
    @Log(title = "小节知识点关联", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(sectionKpService.deleteSectionKpByIds(ids));
    }

    /**
     * 删除小节的所有知识点关联
     */
    @Log(title = "删除小节的所有知识点", businessType = BusinessType.DELETE)
    @DeleteMapping("/section/{sectionId}")
    public AjaxResult removeBySection(@PathVariable Long sectionId)
    {
        return toAjax(sectionKpService.deleteSectionKpBySectionId(sectionId));
    }

    /**
     * 批量新增小节知识点关联（用于AI生成课程结构）
     */
    @Log(title = "批量新增小节知识点关联", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batchInsert(@RequestBody List<SectionKp> sectionKpList)
    {
        int result = sectionKpService.batchInsertSectionKp(sectionKpList);
        if (result > 0) {
            return success(sectionKpList);
        }
        return error("批量创建关联失败");
    }

    /**
     * 为小节批量设置知识点（先删除旧关联，再创建新关联）
     * 
     * @param sectionId 小节ID
     * @param kpIds 知识点ID数组
     */
    @Log(title = "设置小节知识点", businessType = BusinessType.UPDATE)
    @PutMapping("/setKnowledgePoints/{sectionId}")
    public AjaxResult setKnowledgePoints(@PathVariable Long sectionId, @RequestBody Long[] kpIds)
    {
        int result = sectionKpService.setSectionKnowledgePoints(sectionId, kpIds);
        return toAjax(result);
    }
}
