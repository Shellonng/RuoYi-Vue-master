package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.CourseResourceMapper;
import com.ruoyi.system.domain.CourseResource;
import com.ruoyi.system.service.ICourseResourceService;

/**
 * 课程资源 服务层实现
 *
 * @author ruoyi
 */
@Service
public class CourseResourceServiceImpl implements ICourseResourceService
{
    @Autowired
    private CourseResourceMapper courseResourceMapper;

    /**
     * 查询课程资源信息
     *
     * @param id 课程资源ID
     * @return 课程资源信息
     */
    @Override
    public CourseResource selectCourseResourceById(Long id)
    {
        return courseResourceMapper.selectCourseResourceById(id);
    }

    /**
     * 查询课程资源列表
     *
     * @param courseResource 课程资源信息
     * @return 课程资源集合
     */
    @Override
    public List<CourseResource> selectCourseResourceList(CourseResource courseResource)
    {
        return courseResourceMapper.selectCourseResourceList(courseResource);
    }

    /**
     * 新增课程资源
     *
     * @param courseResource 课程资源信息
     * @return 结果
     */
    @Override
    public int insertCourseResource(CourseResource courseResource)
    {
        return courseResourceMapper.insertCourseResource(courseResource);
    }

    /**
     * 修改课程资源
     *
     * @param courseResource 课程资源信息
     * @return 结果
     */
    @Override
    public int updateCourseResource(CourseResource courseResource)
    {
        return courseResourceMapper.updateCourseResource(courseResource);
    }

    /**
     * 删除课程资源信息
     *
     * @param id 课程资源ID
     * @return 结果
     */
    @Override
    public int deleteCourseResourceById(Long id)
    {
        return courseResourceMapper.deleteCourseResourceById(id);
    }

    /**
     * 批量删除课程资源信息
     *
     * @param ids 需要删除的课程资源ID
     * @return 结果
     */
    @Override
    public int deleteCourseResourceByIds(Long[] ids)
    {
        return courseResourceMapper.deleteCourseResourceByIds(ids);
    }

    /**
     * 根据知识点ID查询关联的课程资源
     *
     * @param kpId 知识点ID
     * @return 课程资源集合
     */
    @Override
    public List<CourseResource> selectCourseResourcesByKpId(Long kpId)
    {
        return courseResourceMapper.selectCourseResourcesByKpId(kpId);
    }
}
