package com.ruoyi.web.controller.system;

import java.util.List;
import java.util.Map;
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
import com.ruoyi.system.domain.Section;
import com.ruoyi.system.service.ISectionService;
import com.ruoyi.system.service.ISectionKpService;

/**
 * 课程小节 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/section")
public class SectionController extends BaseController
{
    @Autowired
    private ISectionService sectionService;
    
    @Autowired
    private ISectionKpService sectionKpService;

    /**
     * 获取小节列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Section section)
    {
        startPage();
        List<Section> list = sectionService.selectSectionList(section);
        return getDataTable(list);
    }

    /**
     * 根据章节ID获取小节列表
     */
    @GetMapping("/listByChapter/{chapterId}")
    public AjaxResult listByChapter(@PathVariable Long chapterId)
    {
        List<Section> list = sectionService.selectSectionListByChapterId(chapterId);
        return success(list);
    }

    /**
     * 根据小节编号获取详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(sectionService.selectSectionById(id));
    }

    /**
     * 根据视频URL查找对应的小节
     */
    @GetMapping("/findByVideoUrl")
    public AjaxResult findByVideoUrl(String videoUrl)
    {
        if (videoUrl == null || videoUrl.isEmpty()) {
            return error("视频URL不能为空");
        }
        Section section = sectionService.selectSectionByVideoUrl(videoUrl);
        if (section != null) {
            return success(section);
        }
        return error("未找到对应的小节");
    }

    /**
     * 新增小节
     */
    @Log(title = "课程小节", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Section section)
    {
        int result = sectionService.insertSection(section);
        if (result > 0) {
            return success(section);  // 返回新增的小节对象（包含自动生成的ID）
        }
        return error("新增小节失败");
    }

    /**
     * 修改小节
     */
    @Log(title = "课程小节", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Section section)
    {
        return toAjax(sectionService.updateSection(section));
    }

    /**
     * 删除小节
     */
    @Log(title = "课程小节", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(sectionService.deleteSectionByIds(ids));
    }
    
    /**
     * 为小节关联知识点
     * 用于资源打标功能，将匹配的知识点关联到指定小节
     */
    @Log(title = "小节关联知识点", businessType = BusinessType.UPDATE)
    @PostMapping("/setKnowledgePoints")
    public AjaxResult setKnowledgePoints(@RequestBody Map<String, Object> params)
    {
        Long sectionId = Long.valueOf(params.get("sectionId").toString());
        @SuppressWarnings("unchecked")
        List<Integer> kpIdsInt = (List<Integer>) params.get("kpIds");
        Long[] kpIds = kpIdsInt.stream().map(Long::valueOf).toArray(Long[]::new);
        
        int result = sectionKpService.setSectionKnowledgePoints(sectionId, kpIds);
        if (result > 0) {
            return success("成功关联 " + kpIds.length + " 个知识点到小节");
        }
        return error("关联知识点失败");
    }
}
