package com.ruoyi.system.service;

import com.ruoyi.system.domain.User;

/**
 * 业务用户Service接口
 *
 * @author ruoyi
 */
public interface IUserService
{
    /**
     * 通过sys_user_id查询业务用户
     *
     * @param sysUserId sys_user表的user_id
     * @return 业务用户对象
     */
    public User selectUserBySysUserId(Long sysUserId);

    /**
     * 通过用户名查询业务用户
     *
     * @param username 用户名
     * @return 业务用户对象
     */
    public User selectUserByUsername(String username);

    /**
     * 新增业务用户
     *
     * @param user 业务用户信息
     * @return 结果
     */
    public int insertUser(User user);

    /**
     * 修改业务用户
     *
     * @param user 业务用户信息
     * @return 结果
     */
    public int updateUser(User user);

    /**
     * 根据sys_user_id修改业务用户
     *
     * @param user 业务用户信息
     * @return 结果
     */
    public int updateUserBySysUserId(User user);

    /**
     * 根据sys_user_id删除业务用户
     *
     * @param sysUserId sys_user表的user_id
     * @return 结果
     */
    public int deleteUserBySysUserId(Long sysUserId);
}
