package com.ruoyi.system.service;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.User;

/**
 * 用户同步服务接口
 * 用于同步 sys_user 和 user 两个表的数据
 * 
 * @author ruoyi
 */
public interface IUserSyncService
{
    /**
     * 同步创建用户 - 从 sys_user 同步到 user
     * 
     * @param sysUser sys_user表的用户信息
     * @return 结果
     */
    public int syncCreateUser(SysUser sysUser);

    /**
     * 同步更新用户 - 从 sys_user 同步到 user
     * 
     * @param sysUser sys_user表的用户信息
     * @return 结果
     */
    public int syncUpdateUser(SysUser sysUser);

    /**
     * 同步删除用户 - 从 sys_user 同步到 user
     * 
     * @param userId sys_user表的user_id
     * @return 结果
     */
    public int syncDeleteUser(Long userId);

    /**
     * 根据sys_user_id查询user表的用户
     * 
     * @param sysUserId sys_user表的user_id
     * @return user表的用户对象
     */
    public User getUserBySysUserId(Long sysUserId);

    /**
     * 将SysUser对象转换为User对象
     * 
     * @param sysUser sys_user表的用户信息
     * @return user表的用户对象
     */
    public User convertSysUserToUser(SysUser sysUser);
}

