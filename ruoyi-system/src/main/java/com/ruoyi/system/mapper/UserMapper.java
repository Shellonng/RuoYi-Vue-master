package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.User;
import org.apache.ibatis.annotations.Param;

/**
 * 用户表(user) 数据层
 * 
 * @author ruoyi
 */
public interface UserMapper
{
    /**
     * 根据sys_user_id查询用户
     * 
     * @param sysUserId sys_user表的user_id
     * @return 用户对象
     */
    public User selectUserBySysUserId(@Param("sysUserId") Long sysUserId);

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户对象
     */
    public User selectUserByUsername(@Param("username") String username);

    /**
     * 新增用户
     * 
     * @param user 用户信息
     * @return 结果
     */
    public int insertUser(User user);

    /**
     * 修改用户
     * 
     * @param user 用户信息
     * @return 结果
     */
    public int updateUser(User user);

    /**
     * 根据sys_user_id修改用户
     * 
     * @param user 用户信息
     * @return 结果
     */
    public int updateUserBySysUserId(User user);

    /**
     * 根据sys_user_id删除用户
     * 
     * @param sysUserId sys_user表的user_id
     * @return 结果
     */
    public int deleteUserBySysUserId(@Param("sysUserId") Long sysUserId);
}

