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
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.KnowledgePoint;
import com.ruoyi.system.service.IKnowledgePointService;

/**
 * 知识点 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/knowledgePoint")
public class KnowledgePointController extends BaseController
{
    @Autowired
    private IKnowledgePointService knowledgePointService;

    /**
     * 获取知识点列表
     */
    @GetMapping("/list")
    public TableDataInfo list(KnowledgePoint knowledgePoint)
    {
        startPage();
        List<KnowledgePoint> list = knowledgePointService.selectKnowledgePointList(knowledgePoint);
        return getDataTable(list);
    }

    /**
     * 根据课程ID获取知识点列表
     */
    @GetMapping("/listByCourse/{courseId}")
    public AjaxResult listByCourse(@PathVariable Long courseId)
    {
        List<KnowledgePoint> list = knowledgePointService.selectKnowledgePointListByCourseId(courseId);
        return success(list);
    }

    /**
     * 根据课程ID和难度级别获取知识点列表
     */
    @GetMapping("/listByCourse/{courseId}/level/{level}")
    public AjaxResult listByCourseAndLevel(@PathVariable Long courseId, @PathVariable String level)
    {
        List<KnowledgePoint> list = knowledgePointService.selectKnowledgePointListByCourseIdAndLevel(courseId, level);
        return success(list);
    }

    /**
     * 根据知识点编号获取详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(knowledgePointService.selectKnowledgePointById(id));
    }

    /**
     * 新增知识点
     */
    @Log(title = "知识点", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody KnowledgePoint knowledgePoint)
    {
        // 自动设置创建者为当前登录用户
        if (knowledgePoint.getCreatorUserId() == null)
        {
            knowledgePoint.setCreatorUserId(SecurityUtils.getUserId());
        }
        int result = knowledgePointService.insertKnowledgePoint(knowledgePoint);
        if (result > 0) {
            return success(knowledgePoint);  // 返回新增的知识点对象（包含自动生成的ID）
        }
        return error("新增知识点失败");
    }

    /**
     * 修改知识点
     */
    @Log(title = "知识点", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody KnowledgePoint knowledgePoint)
    {
        return toAjax(knowledgePointService.updateKnowledgePoint(knowledgePoint));
    }

    /**
     * 删除知识点
     */
    @Log(title = "知识点", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(knowledgePointService.deleteKnowledgePointByIds(ids));
    }

    /**
     * 批量插入知识点（用于AI生成课程结构）
     */
    @Log(title = "批量新增知识点", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batchInsert(@RequestBody List<KnowledgePoint> knowledgePointList)
    {
        // 为每个知识点设置创建者
        Long currentUserId = SecurityUtils.getUserId();
        for (KnowledgePoint kp : knowledgePointList)
        {
            if (kp.getCreatorUserId() == null)
            {
                kp.setCreatorUserId(currentUserId);
            }
        }
        
        int result = knowledgePointService.batchInsertKnowledgePoints(knowledgePointList);
        if (result > 0) {
            return success(knowledgePointList);
        }
        return error("批量创建知识点失败");
    }
}
