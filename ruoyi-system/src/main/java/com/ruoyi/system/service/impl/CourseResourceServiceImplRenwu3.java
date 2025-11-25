package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.CourseResourceRenwu3;
import com.ruoyi.system.mapper.CourseResourceMapperRenwu3;
import com.ruoyi.system.service.ICourseResourceServiceRenwu3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程资源Service业务层处理（任务3-资源智能打标）
 * 
 * @author ruoyi
 * @date 2025-01-18
 */
@Service
public class CourseResourceServiceImplRenwu3 implements ICourseResourceServiceRenwu3
{
    @Autowired
    private CourseResourceMapperRenwu3 courseResourceMapper;

    /**
     * 查询课程资源
     * 
     * @param id 课程资源主键
     * @return 课程资源
     */
    @Override
    public CourseResourceRenwu3 selectCourseResourceById(Long id)
    {
        return courseResourceMapper.selectCourseResourceById(id);
    }

    /**
     * 查询课程资源列表
     * 
     * @param courseResource 课程资源
     * @return 课程资源
     */
    @Override
    public List<CourseResourceRenwu3> selectCourseResourceList(CourseResourceRenwu3 courseResource)
    {
        return courseResourceMapper.selectCourseResourceList(courseResource);
    }

    /**
     * 新增课程资源
     * 
     * @param courseResource 课程资源
     * @return 结果
     */
    @Override
    public int insertCourseResource(CourseResourceRenwu3 courseResource)
    {
        return courseResourceMapper.insertCourseResource(courseResource);
    }

    /**
     * 修改课程资源
     * 
     * @param courseResource 课程资源
     * @return 结果
     */
    @Override
    public int updateCourseResource(CourseResourceRenwu3 courseResource)
    {
        return courseResourceMapper.updateCourseResource(courseResource);
    }

    /**
     * 批量删除课程资源
     * 
     * @param ids 需要删除的课程资源主键
     * @return 结果
     */
    @Override
    public int deleteCourseResourceByIds(Long[] ids)
    {
        return courseResourceMapper.deleteCourseResourceByIds(ids);
    }

    /**
     * 删除课程资源信息
     * 
     * @param id 课程资源主键
     * @return 结果
     */
    @Override
    public int deleteCourseResourceById(Long id)
    {
        return courseResourceMapper.deleteCourseResourceById(id);
    }
}
