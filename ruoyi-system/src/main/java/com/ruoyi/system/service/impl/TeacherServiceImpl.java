package com.ruoyi.system.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.Teacher;
import com.ruoyi.system.mapper.TeacherMapper;
import com.ruoyi.system.service.ITeacherService;
import com.ruoyi.system.utils.BusinessUserUtils;

/**
 * 教师信息Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class TeacherServiceImpl implements ITeacherService
{
    @Autowired
    private TeacherMapper teacherMapper;

    /**
     * 查询教师信息
     * 
     * @param id 教师ID
     * @return 教师信息
     */
    @Override
    public Teacher selectTeacherById(Long id)
    {
        return teacherMapper.selectTeacherById(id);
    }

    /**
     * 根据用户ID查询教师信息
     * 
     * @param userId 用户ID
     * @return 教师信息
     */
    @Override
    public Teacher selectTeacherByUserId(Long userId)
    {
        return teacherMapper.selectTeacherByUserId(userId);
    }

    /**
     * 获取当前登录教师的信息
     * 
     * @return 教师信息
     */
    @Override
    public Teacher getCurrentTeacherInfo()
    {
        Long userId = BusinessUserUtils.getCurrentBusinessUserId();
        return teacherMapper.selectTeacherByUserId(userId);
    }

    /**
     * 新增教师信息
     * 
     * @param teacher 教师信息
     * @return 结果
     */
    @Override
    public int insertTeacher(Teacher teacher)
    {
        return teacherMapper.insertTeacher(teacher);
    }

    /**
     * 修改教师信息
     * 
     * @param teacher 教师信息
     * @return 结果
     */
    @Override
    public int updateTeacher(Teacher teacher)
    {
        return teacherMapper.updateTeacher(teacher);
    }

    /**
     * 删除教师信息
     * 
     * @param id 教师ID
     * @return 结果
     */
    @Override
    public int deleteTeacherById(Long id)
    {
        return teacherMapper.deleteTeacherById(id);
    }
}
