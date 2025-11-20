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
import com.ruoyi.system.domain.Section;
import com.ruoyi.system.service.ISectionService;

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
}
