package com.ruoyi.system.service.impl;

import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.User;
import com.ruoyi.system.mapper.UserMapper;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.IUserSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户同步服务实现
 * 
 * @author ruoyi
 */
@Service
public class UserSyncServiceImpl implements IUserSyncService
{
    private static final Logger log = LoggerFactory.getLogger(UserSyncServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ISysRoleService roleService;

    /**
     * 同步创建用户 - 从 sys_user 同步到 user
     * 
     * @param sysUser sys_user表的用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int syncCreateUser(SysUser sysUser)
    {
        try
        {
            // 检查是否已存在
            User existUser = userMapper.selectUserBySysUserId(sysUser.getUserId());
            if (existUser != null)
            {
                log.warn("用户已存在，sys_user_id: {}", sysUser.getUserId());
                return 0;
            }

            // 转换并插入
            User user = convertSysUserToUser(sysUser);
            int result = userMapper.insertUser(user);
            
            if (result > 0)
            {
                log.info("同步创建用户成功: sys_user_id={}, username={}", sysUser.getUserId(), sysUser.getUserName());
            }
            
            return result;
        }
        catch (Exception e)
        {
            log.error("同步创建用户失败: sys_user_id={}, error={}", sysUser.getUserId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 同步更新用户 - 从 sys_user 同步到 user
     * 
     * @param sysUser sys_user表的用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int syncUpdateUser(SysUser sysUser)
    {
        try
        {
            // 检查是否存在
            User existUser = userMapper.selectUserBySysUserId(sysUser.getUserId());
            if (existUser == null)
            {
                log.warn("用户不存在，尝试创建: sys_user_id={}", sysUser.getUserId());
                return syncCreateUser(sysUser);
            }

            // 转换并更新
            User user = convertSysUserToUser(sysUser);
            user.setId(existUser.getId()); // 保留原有ID
            int result = userMapper.updateUserBySysUserId(user);
            
            if (result > 0)
            {
                log.info("同步更新用户成功: sys_user_id={}, username={}", sysUser.getUserId(), sysUser.getUserName());
            }
            
            return result;
        }
        catch (Exception e)
        {
            log.error("同步更新用户失败: sys_user_id={}, error={}", sysUser.getUserId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 同步删除用户 - 从 sys_user 同步到 user (软删除)
     * 
     * @param userId sys_user表的user_id
     * @return 结果
     */
    @Override
    @Transactional
    public int syncDeleteUser(Long userId)
    {
        try
        {
            // 软删除：更新状态为DELETED
            User user = new User();
            user.setSysUserId(userId);
            user.setStatus("DELETED");
            int result = userMapper.updateUserBySysUserId(user);
            
            if (result > 0)
            {
                log.info("同步删除用户成功: sys_user_id={}", userId);
            }
            
            return result;
        }
        catch (Exception e)
        {
            log.error("同步删除用户失败: sys_user_id={}, error={}", userId, e.getMessage());
            throw e;
        }
    }

    /**
     * 根据sys_user_id查询user表的用户
     * 
     * @param sysUserId sys_user表的user_id
     * @return user表的用户对象
     */
    @Override
    public User getUserBySysUserId(Long sysUserId)
    {
        return userMapper.selectUserBySysUserId(sysUserId);
    }

    /**
     * 将SysUser对象转换为User对象
     *
     * @param sysUser sys_user表的用户信息
     * @return user表的用户对象
     */
    @Override
    public User convertSysUserToUser(SysUser sysUser)
    {
        User user = new User();
        user.setSysUserId(sysUser.getUserId());
        user.setUsername(sysUser.getUserName());
        user.setPassword(sysUser.getPassword());
        user.setEmail(sysUser.getEmail());
        user.setRealName(sysUser.getNickName());
        user.setAvatar(sysUser.getAvatar());

        // 状态转换: sys_user.status (0=正常, 1=停用) -> user.status (ACTIVE, INACTIVE)
        if ("0".equals(sysUser.getStatus()))
        {
            user.setStatus("ACTIVE");
        }
        else if ("1".equals(sysUser.getStatus()))
        {
            user.setStatus("INACTIVE");
        }
        else
        {
            user.setStatus("ACTIVE");
        }

        // 角色转换: 从 sys_user_role 表查询用户角色，映射到 user.role
        user.setRole(determineUserRole(sysUser.getUserId()));

        return user;
    }

    /**
     * 根据用户ID确定用户角色
     * 从 sys_user_role 和 sys_role 表中查询用户的角色，并映射到 user.role
     *
     * @param userId 用户ID
     * @return 用户角色 (STUDENT, TEACHER, ADMIN)
     */
    private String determineUserRole(Long userId)
    {
        try
        {
            // 查询用户的所有角色
            List<SysRole> roles = roleService.selectRolesByUserId(userId);

            if (roles == null || roles.isEmpty())
            {
                log.warn("用户没有分配角色，默认为STUDENT: userId={}", userId);
                return "STUDENT";
            }

            // 角色优先级: ADMIN > TEACHER > STUDENT
            // 根据 role_key 或 role_name 判断
            for (SysRole role : roles)
            {
                String roleKey = role.getRoleKey();
                String roleName = role.getRoleName();

                // 判断是否为管理员角色
                if (role.isAdmin() ||
                    "admin".equalsIgnoreCase(roleKey) ||
                    "管理员".equals(roleName))
                {
                    return "ADMIN";
                }
            }

            // 判断是否为教师角色
            for (SysRole role : roles)
            {
                String roleKey = role.getRoleKey();
                String roleName = role.getRoleName();

                if ("teacher".equalsIgnoreCase(roleKey) ||
                    "教师".equals(roleName) ||
                    roleName.contains("教师"))
                {
                    return "TEACHER";
                }
            }

            // 判断是否为学生角色
            for (SysRole role : roles)
            {
                String roleKey = role.getRoleKey();
                String roleName = role.getRoleName();

                if ("student".equalsIgnoreCase(roleKey) ||
                    "学生".equals(roleName) ||
                    roleName.contains("学生"))
                {
                    return "STUDENT";
                }
            }

            // 默认为学生
            log.info("用户角色无法映射，默认为STUDENT: userId={}, roles={}", userId, roles);
            return "STUDENT";
        }
        catch (Exception e)
        {
            log.error("查询用户角色失败，默认为STUDENT: userId={}, error={}", userId, e.getMessage());
            return "STUDENT";
        }
    }
}

