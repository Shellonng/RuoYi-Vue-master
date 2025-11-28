package com.ruoyi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.Teacher;
import com.ruoyi.system.service.ITeacherService;
import com.ruoyi.system.utils.BusinessUserUtils;

/**
 * 教师信息Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/teacher")
public class TeacherController extends BaseController
{
    @Autowired
    private ITeacherService teacherService;

    /**
     * 获取当前登录教师的信息
     */
    @GetMapping("/profile")
    public AjaxResult getProfile()
    {
        Teacher teacher = teacherService.getCurrentTeacherInfo();
        if (teacher == null)
        {
            // 如果没有教师信息，返回一个空对象
            teacher = new Teacher();
            Long userId = BusinessUserUtils.getCurrentBusinessUserId();
            teacher.setUserId(userId);
        }
        return success(teacher);
    }

    /**
     * 根据教师ID获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:teacher:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(teacherService.selectTeacherById(id));
    }

    /**
     * 根据用户ID获取教师信息
     */
    @GetMapping("/byUserId/{userId}")
    public AjaxResult getInfoByUserId(@PathVariable("userId") Long userId)
    {
        return success(teacherService.selectTeacherByUserId(userId));
    }

    /**
     * 新增教师信息
     */
    @PreAuthorize("@ss.hasPermi('system:teacher:add')")
    @Log(title = "教师信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Teacher teacher)
    {
        return toAjax(teacherService.insertTeacher(teacher));
    }

    /**
     * 修改教师信息
     */
    @Log(title = "教师信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Teacher teacher)
    {
        // 如果没有ID，尝试通过userId查找
        if (teacher.getId() == null && teacher.getUserId() != null)
        {
            Teacher existingTeacher = teacherService.selectTeacherByUserId(teacher.getUserId());
            if (existingTeacher != null)
            {
                teacher.setId(existingTeacher.getId());
            }
        }

        // 如果找到了ID，执行更新；否则执行插入
        if (teacher.getId() != null)
        {
            return toAjax(teacherService.updateTeacher(teacher));
        }
        else
        {
            // 如果没有userId，使用当前登录用户的ID
            if (teacher.getUserId() == null)
            {
                teacher.setUserId(BusinessUserUtils.getCurrentBusinessUserId());
            }
            return toAjax(teacherService.insertTeacher(teacher));
        }
    }

    /**
     * 删除教师信息
     */
    @PreAuthorize("@ss.hasPermi('system:teacher:remove')")
    @Log(title = "教师信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        return toAjax(teacherService.deleteTeacherById(id));
    }
}
