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
import com.ruoyi.system.domain.CourseResource;
import com.ruoyi.system.service.ICourseResourceService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 课程资源Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/courseResource")
public class CourseResourceController extends BaseController
{
    @Autowired
    private ICourseResourceService courseResourceService;

    /**
     * 查询课程资源列表
     */
    @GetMapping("/list")
    public TableDataInfo list(CourseResource courseResource)
    {
        startPage();
        List<CourseResource> list = courseResourceService.selectCourseResourceList(courseResource);
        return getDataTable(list);
    }

    /**
     * 导出课程资源列表
     */
    @Log(title = "课程资源", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CourseResource courseResource)
    {
        List<CourseResource> list = courseResourceService.selectCourseResourceList(courseResource);
        ExcelUtil<CourseResource> util = new ExcelUtil<CourseResource>(CourseResource.class);
        util.exportExcel(response, list, "课程资源数据");
    }

    /**
     * 获取课程资源详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(courseResourceService.selectCourseResourceById(id));
    }

    /**
     * 根据知识点ID查询关联的课程资源
     */
    @GetMapping("/byKnowledgePoint/{kpId}")
    public AjaxResult getResourcesByKnowledgePoint(@PathVariable("kpId") Long kpId)
    {
        List<CourseResource> list = courseResourceService.selectCourseResourcesByKpId(kpId);
        return success(list);
    }

    /**
     * 新增课程资源
     */
    @Log(title = "课程资源", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CourseResource courseResource)
    {
        return toAjax(courseResourceService.insertCourseResource(courseResource));
    }

    /**
     * 修改课程资源
     */
    @Log(title = "课程资源", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CourseResource courseResource)
    {
        return toAjax(courseResourceService.updateCourseResource(courseResource));
    }

    /**
     * 删除课程资源
     */
    @Log(title = "课程资源", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(courseResourceService.deleteCourseResourceByIds(ids));
    }
}
