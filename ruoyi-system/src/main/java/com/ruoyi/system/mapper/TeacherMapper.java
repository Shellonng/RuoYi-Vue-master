package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.Teacher;
import org.apache.ibatis.annotations.Param;

/**
 * 教师信息Mapper接口
 * 
 * @author ruoyi
 */
public interface TeacherMapper
{
    /**
     * 查询教师信息
     * 
     * @param id 教师ID
     * @return 教师信息
     */
    public Teacher selectTeacherById(Long id);

    /**
     * 根据用户ID查询教师信息
     * 
     * @param userId 用户ID
     * @return 教师信息
     */
    public Teacher selectTeacherByUserId(@Param("userId") Long userId);

    /**
     * 新增教师信息
     * 
     * @param teacher 教师信息
     * @return 结果
     */
    public int insertTeacher(Teacher teacher);

    /**
     * 修改教师信息
     * 
     * @param teacher 教师信息
     * @return 结果
     */
    public int updateTeacher(Teacher teacher);

    /**
     * 删除教师信息
     * 
     * @param id 教师ID
     * @return 结果
     */
    public int deleteTeacherById(Long id);
}
