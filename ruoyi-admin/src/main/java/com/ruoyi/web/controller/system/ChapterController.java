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
import com.ruoyi.system.domain.Chapter;
import com.ruoyi.system.service.IChapterService;

/**
 * 课程章节 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/chapter")
public class ChapterController extends BaseController
{
    @Autowired
    private IChapterService chapterService;

    /**
     * 获取章节列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Chapter chapter)
    {
        startPage();
        List<Chapter> list = chapterService.selectChapterList(chapter);
        return getDataTable(list);
    }

    /**
     * 根据课程ID获取章节列表
     */
    @GetMapping("/listByCourse/{courseId}")
    public AjaxResult listByCourse(@PathVariable Long courseId)
    {
        List<Chapter> list = chapterService.selectChapterListByCourseId(courseId);
        return success(list);
    }

    /**
     * 根据章节编号获取详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(chapterService.selectChapterById(id));
    }

    /**
     * 新增章节
     */
    @Log(title = "课程章节", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Chapter chapter)
    {
        int result = chapterService.insertChapter(chapter);
        if (result > 0) {
            return success(chapter);  // 返回新增的章节对象（包含自动生成的ID）
        }
        return error("新增章节失败");
    }

    /**
     * 修改章节
     */
    @Log(title = "课程章节", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Chapter chapter)
    {
        return toAjax(chapterService.updateChapter(chapter));
    }

    /**
     * 删除章节
     */
    @Log(title = "课程章节", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(chapterService.deleteChapterByIds(ids));
    }
}
