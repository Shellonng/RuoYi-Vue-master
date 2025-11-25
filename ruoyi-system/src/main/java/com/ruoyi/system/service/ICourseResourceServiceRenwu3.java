package com.ruoyi.system.service;

import com.ruoyi.system.domain.CourseResourceRenwu3;
import java.util.List;

/**
 * 课程资源Service接口（任务3-资源智能打标）
 * 
 * @author ruoyi
 * @date 2025-01-18
 */
public interface ICourseResourceServiceRenwu3
{
    /**
     * 查询课程资源
     * 
     * @param id 课程资源主键
     * @return 课程资源
     */
    public CourseResourceRenwu3 selectCourseResourceById(Long id);

    /**
     * 查询课程资源列表
     * 
     * @param courseResource 课程资源
     * @return 课程资源集合
     */
    public List<CourseResourceRenwu3> selectCourseResourceList(CourseResourceRenwu3 courseResource);

    /**
     * 新增课程资源
     * 
     * @param courseResource 课程资源
     * @return 结果
     */
    public int insertCourseResource(CourseResourceRenwu3 courseResource);

    /**
     * 修改课程资源
     * 
     * @param courseResource 课程资源
     * @return 结果
     */
    public int updateCourseResource(CourseResourceRenwu3 courseResource);

    /**
     * 批量删除课程资源
     * 
     * @param ids 需要删除的课程资源主键集合
     * @return 结果
     */
    public int deleteCourseResourceByIds(Long[] ids);

    /**
     * 删除课程资源信息
     * 
     * @param id 课程资源主键
     * @return 结果
     */
    public int deleteCourseResourceById(Long id);
}
