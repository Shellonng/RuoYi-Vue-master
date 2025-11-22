package com.ruoyi.web.controller.system;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.ruoyi.system.domain.SectionComment;
import com.ruoyi.system.domain.Section;
import com.ruoyi.system.domain.Course;
import com.ruoyi.system.domain.User;
import com.ruoyi.system.service.ISectionCommentService;
import com.ruoyi.system.service.ISectionService;
import com.ruoyi.system.service.ICourseService;
import com.ruoyi.system.utils.BusinessUserUtils;

/**
 * 小节评论Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/comment")
public class SectionCommentController extends BaseController
{
    @Autowired
    private ISectionCommentService sectionCommentService;
    
    @Autowired
    private ISectionService sectionService;
    
    @Autowired
    private ICourseService courseService;

    /**
     * 查询小节评论列表
     */
    @GetMapping("/list")
    public AjaxResult list(SectionComment sectionComment)
    {
        List<SectionComment> list = sectionCommentService.selectSectionCommentList(sectionComment);
        return success(list);
    }

    /**
     * 根据小节ID查询评论树形列表
     */
    @GetMapping("/tree/{sectionId}")
    public AjaxResult getCommentTree(@PathVariable("sectionId") Long sectionId)
    {
        List<SectionComment> list = sectionCommentService.selectCommentTreeBySectionId(sectionId);
        return success(list);
    }

    /**
     * 获取当前登录的业务用户信息
     */
    @GetMapping("/currentUser")
    public AjaxResult getCurrentUser()
    {
        User currentUser = BusinessUserUtils.getCurrentBusinessUser();
        if (currentUser == null)
        {
            return error("未找到当前用户信息");
        }
        return success(currentUser);
    }

    /**
     * 获取小节评论详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(sectionCommentService.selectSectionCommentById(id));
    }

    /**
     * 新增小节评论
     */
    @Log(title = "小节评论", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SectionComment sectionComment)
    {
        // 从token获取当前业务用户ID
        Long userId = BusinessUserUtils.getCurrentBusinessUserId();
        sectionComment.setUserId(userId);
        
        return toAjax(sectionCommentService.insertSectionComment(sectionComment));
    }

    /**
     * 修改小节评论
     */
    @Log(title = "小节评论", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SectionComment sectionComment)
    {
        // 验证是否是评论作者本人
        Long userId = BusinessUserUtils.getCurrentBusinessUserId();
        SectionComment existingComment = sectionCommentService.selectSectionCommentById(sectionComment.getId());
        
        if (existingComment == null)
        {
            return error("评论不存在");
        }
        
        if (!existingComment.getUserId().equals(userId))
        {
            return error("只能修改自己的评论");
        }
        
        return toAjax(sectionCommentService.updateSectionComment(sectionComment));
    }

    /**
     * 删除小节评论
     */
    @Log(title = "小节评论", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        // 获取当前业务用户ID
        Long userId = BusinessUserUtils.getCurrentBusinessUserId();
        boolean isAdmin = BusinessUserUtils.isAdmin();
        
        // 遍历所有要删除的评论，验证权限
        for (Long id : ids)
        {
            SectionComment comment = sectionCommentService.selectSectionCommentById(id);
            if (comment == null)
            {
                continue;
            }
            
            // 1. 管理员可以删除任何评论
            if (isAdmin)
            {
                continue;
            }
            
            // 2. 评论作者本人可以删除
            if (comment.getUserId().equals(userId))
            {
                continue;
            }
            
            // 3. 课程创建者（教师）可以删除该课程下的所有评论
            Long sectionId = comment.getSectionId();
            Section section = sectionService.selectSectionById(sectionId);
            if (section != null && section.getCourseId() != null)
            {
                Course course = courseService.selectCourseById(section.getCourseId());
                if (course != null && userId.equals(course.getTeacherUserId()))
                {
                    continue; // 是课程创建者，有权限删除
                }
            }
            
            // 如果以上条件都不满足，则无权限删除
            return error("您没有权限删除该评论");
        }
        
        return toAjax(sectionCommentService.deleteSectionCommentByIds(ids));
    }
}
