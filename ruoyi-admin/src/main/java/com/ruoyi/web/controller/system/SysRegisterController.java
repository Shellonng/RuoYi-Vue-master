package com.ruoyi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.RegisterBody;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.SysRegisterService;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.IUserSyncService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.domain.User;
import com.ruoyi.common.core.domain.entity.SysUser;

/**
 * 注册验证
 * 
 * @author ruoyi
 */
@RestController
public class SysRegisterController extends BaseController
{
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private IUserSyncService userSyncService;

    @Autowired
    private ISysUserService userService;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }

    /**
     * 验证用户同步状态
     *
     * @param username 用户名
     * @return 同步状态信息
     */
    @GetMapping("/verifySyncStatus")
    public AjaxResult verifySyncStatus(@RequestParam("username") String username)
    {
        try {
            // 查询 sys_user 表
            SysUser sysUser = userService.selectUserByUserName(username);
            if (sysUser == null) {
                return error("用户不存在");
            }

            // 查询 user 表
            User user = userSyncService.getUserBySysUserId(sysUser.getUserId());

            AjaxResult ajax = AjaxResult.success();
            ajax.put("sysUser", sysUser);
            ajax.put("user", user);
            ajax.put("synced", user != null);

            if (user != null) {
                // 验证关键字段
                boolean usernameMatch = username.equals(user.getUsername());
                boolean sysUserIdMatch = sysUser.getUserId().equals(user.getSysUserId());

                ajax.put("usernameMatch", usernameMatch);
                ajax.put("sysUserIdMatch", sysUserIdMatch);
                ajax.put("syncSuccess", usernameMatch && sysUserIdMatch);
            } else {
                ajax.put("syncSuccess", false);
                ajax.put("message", "user表中未找到对应记录");
            }

            return ajax;
        } catch (Exception e) {
            return error("验证同步状态失败: " + e.getMessage());
        }
    }
}
