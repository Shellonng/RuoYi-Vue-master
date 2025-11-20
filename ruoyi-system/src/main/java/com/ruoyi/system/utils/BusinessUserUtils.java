package com.ruoyi.system.utils;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.User;
import com.ruoyi.system.service.IUserService;
import org.springframework.stereotype.Component;

/**
 * 业务用户工具类
 *
 * 用于获取当前登录用户的业务用户ID（user.id）
 *
 * 使用方法：
 * Long userId = BusinessUserUtils.getCurrentBusinessUserId();
 *
 * @author ruoyi
 */
@Component
public class BusinessUserUtils
{
    private static IUserService userService;

    /**
     * 通过构造函数注入（解决静态方法无法注入的问题）
     */
    public BusinessUserUtils(IUserService userService)
    {
        BusinessUserUtils.userService = userService;
    }

    /**
     * 获取当前业务用户ID（user.id）
     *
     * 说明：
     * - SecurityUtils.getUserId() 返回的是 sys_user.user_id
     * - 本方法返回的是 user.id（业务表的外键）
     *
     * @return 业务用户ID
     */
    public static Long getCurrentBusinessUserId()
    {
        // 从token获取 sys_user_id
        Long sysUserId = SecurityUtils.getUserId();

        // 查询业务用户表，获取 user.id
        User user = userService.selectUserBySysUserId(sysUserId);

        if (user == null)
        {
            throw new ServiceException("业务用户不存在");
        }

        return user.getId();
    }

    /**
     * 获取当前业务用户完整信息
     *
     * @return 业务用户对象（包含角色、邮箱等信息）
     */
    public static User getCurrentBusinessUser()
    {
        Long sysUserId = SecurityUtils.getUserId();
        User user = userService.selectUserBySysUserId(sysUserId);

        if (user == null)
        {
            throw new ServiceException("业务用户不存在");
        }

        return user;
    }

    /**
     * 获取当前用户的角色
     *
     * @return 角色（STUDENT/TEACHER/ADMIN）
     */
    public static String getCurrentUserRole()
    {
        return getCurrentBusinessUser().getRole();
    }

    /**
     * 判断当前用户是否为学生
     *
     * @return true-是学生，false-不是学生
     */
    public static boolean isStudent()
    {
        return "STUDENT".equals(getCurrentUserRole());
    }

    /**
     * 判断当前用户是否为教师
     *
     * @return true-是教师，false-不是教师
     */
    public static boolean isTeacher()
    {
        return "TEACHER".equals(getCurrentUserRole());
    }

    /**
     * 判断当前用户是否为管理员
     *
     * @return true-是管理员，false-不是管理员
     */
    public static boolean isAdmin()
    {
        return "ADMIN".equals(getCurrentUserRole());
    }
}
