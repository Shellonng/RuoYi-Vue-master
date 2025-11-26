package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.CourseResourceKp;

/**
 * 课程资源与知识点关联 数据层
 *
 * @author ruoyi
 */
public interface CourseResourceKpMapper
{
    /**
     * 查询课程资源与知识点关联信息
     *
     * @param id 课程资源与知识点关联ID
     * @return 课程资源与知识点关联信息
     */
    public CourseResourceKp selectCourseResourceKpById(Long id);

    /**
     * 查询课程资源与知识点关联列表
     *
     * @param courseResourceKp 课程资源与知识点关联信息
     * @return 课程资源与知识点关联集合
     */
    public List<CourseResourceKp> selectCourseResourceKpList(CourseResourceKp courseResourceKp);

    /**
     * 根据资源ID查询关联的知识点ID列表
     *
     * @param resourceId 资源ID
     * @return 知识点ID列表
     */
    public List<Long> selectKpIdsByResourceId(@Param("resourceId") Long resourceId);

    /**
     * 新增课程资源与知识点关联
     *
     * @param courseResourceKp 课程资源与知识点关联信息
     * @return 结果
     */
    public int insertCourseResourceKp(CourseResourceKp courseResourceKp);

    /**
     * 批量新增课程资源与知识点关联
     *
     * @param list 课程资源与知识点关联列表
     * @return 结果
     */
    public int batchInsertCourseResourceKp(List<CourseResourceKp> list);

    /**
     * 修改课程资源与知识点关联
     *
     * @param courseResourceKp 课程资源与知识点关联信息
     * @return 结果
     */
    public int updateCourseResourceKp(CourseResourceKp courseResourceKp);

    /**
     * 删除课程资源与知识点关联
     *
     * @param id 课程资源与知识点关联ID
     * @return 结果
     */
    public int deleteCourseResourceKpById(Long id);

    /**
     * 根据资源ID删除课程资源与知识点关联
     *
     * @param resourceId 资源ID
     * @return 结果
     */
    public int deleteCourseResourceKpByResourceId(@Param("resourceId") Long resourceId);

    /**
     * 批量删除课程资源与知识点关联
     *
     * @param ids 需要删除的课程资源与知识点关联ID
     * @return 结果
     */
    public int deleteCourseResourceKpByIds(Long[] ids);
}
