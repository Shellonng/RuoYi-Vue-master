package com.ruoyi.web.controller.system;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.utils.BusinessUserUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 业务用户ID查询Controller
 * 用于智能系统集成，获取当前登录用户的业务ID
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/businessUser")
public class UserIdController extends BaseController
{
    /**
     * 获取当前用户的业务ID（user.id）
     * 前端可调用此接口将sys_user.user_id转换为user.id
     */
    @GetMapping("/currentId")
    public AjaxResult getCurrentBusinessUserId()
    {
        Long businessUserId = BusinessUserUtils.getCurrentBusinessUserId();
        return AjaxResult.success(businessUserId);
    }
}
