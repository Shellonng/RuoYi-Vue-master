package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CourseResource;

/**
 * 课程资源 服务层
 *
 * @author ruoyi
 */
public interface ICourseResourceService
{
    /**
     * 查询课程资源信息
     *
     * @param id 课程资源ID
     * @return 课程资源信息
     */
    public CourseResource selectCourseResourceById(Long id);

    /**
     * 查询课程资源列表
     *
     * @param courseResource 课程资源信息
     * @return 课程资源集合
     */
    public List<CourseResource> selectCourseResourceList(CourseResource courseResource);

    /**
     * 新增课程资源
     *
     * @param courseResource 课程资源信息
     * @return 结果
     */
    public int insertCourseResource(CourseResource courseResource);

    /**
     * 修改课程资源
     *
     * @param courseResource 课程资源信息
     * @return 结果
     */
    public int updateCourseResource(CourseResource courseResource);

    /**
     * 删除课程资源信息
     *
     * @param id 课程资源ID
     * @return 结果
     */
    public int deleteCourseResourceById(Long id);

    /**
     * 批量删除课程资源信息
     *
     * @param ids 需要删除的课程资源ID
     * @return 结果
     */
    public int deleteCourseResourceByIds(Long[] ids);

    /**
     * 根据知识点ID查询关联的课程资源
     *
     * @param kpId 知识点ID
     * @return 课程资源集合
     */
    public List<CourseResource> selectCourseResourcesByKpId(Long kpId);
}
