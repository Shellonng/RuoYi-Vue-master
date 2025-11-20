package com.ruoyi.system.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.User;
import com.ruoyi.system.mapper.UserMapper;
import com.ruoyi.system.service.IUserService;

/**
 * 业务用户Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class UserServiceImpl implements IUserService
{
    @Autowired
    private UserMapper userMapper;

    /**
     * 通过sys_user_id查询业务用户
     *
     * @param sysUserId sys_user表的user_id
     * @return 业务用户对象
     */
    @Override
    public User selectUserBySysUserId(Long sysUserId)
    {
        return userMapper.selectUserBySysUserId(sysUserId);
    }

    /**
     * 通过用户名查询业务用户
     *
     * @param username 用户名
     * @return 业务用户对象
     */
    @Override
    public User selectUserByUsername(String username)
    {
        return userMapper.selectUserByUsername(username);
    }

    /**
     * 新增业务用户
     *
     * @param user 业务用户信息
     * @return 结果
     */
    @Override
    public int insertUser(User user)
    {
        return userMapper.insertUser(user);
    }

    /**
     * 修改业务用户
     *
     * @param user 业务用户信息
     * @return 结果
     */
    @Override
    public int updateUser(User user)
    {
        return userMapper.updateUser(user);
    }

    /**
     * 根据sys_user_id修改业务用户
     *
     * @param user 业务用户信息
     * @return 结果
     */
    @Override
    public int updateUserBySysUserId(User user)
    {
        return userMapper.updateUserBySysUserId(user);
    }

    /**
     * 根据sys_user_id删除业务用户
     *
     * @param sysUserId sys_user表的user_id
     * @return 结果
     */
    @Override
    public int deleteUserBySysUserId(Long sysUserId)
    {
        return userMapper.deleteUserBySysUserId(sysUserId);
    }
}
