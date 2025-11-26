package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.mapper.CourseResourceKpMapper;
import com.ruoyi.system.domain.CourseResourceKp;
import com.ruoyi.system.service.ICourseResourceKpService;

/**
 * 课程资源与知识点关联 服务层实现
 *
 * @author ruoyi
 */
@Service
public class CourseResourceKpServiceImpl implements ICourseResourceKpService
{
    @Autowired
    private CourseResourceKpMapper courseResourceKpMapper;

    /**
     * 查询课程资源与知识点关联信息
     *
     * @param id 课程资源与知识点关联ID
     * @return 课程资源与知识点关联信息
     */
    @Override
    public CourseResourceKp selectCourseResourceKpById(Long id)
    {
        return courseResourceKpMapper.selectCourseResourceKpById(id);
    }

    /**
     * 查询课程资源与知识点关联列表
     *
     * @param courseResourceKp 课程资源与知识点关联信息
     * @return 课程资源与知识点关联集合
     */
    @Override
    public List<CourseResourceKp> selectCourseResourceKpList(CourseResourceKp courseResourceKp)
    {
        return courseResourceKpMapper.selectCourseResourceKpList(courseResourceKp);
    }

    /**
     * 根据资源ID查询关联的知识点ID列表
     *
     * @param resourceId 资源ID
     * @return 知识点ID列表
     */
    @Override
    public List<Long> selectKpIdsByResourceId(Long resourceId)
    {
        return courseResourceKpMapper.selectKpIdsByResourceId(resourceId);
    }

    /**
     * 新增课程资源与知识点关联
     *
     * @param courseResourceKp 课程资源与知识点关联信息
     * @return 结果
     */
    @Override
    public int insertCourseResourceKp(CourseResourceKp courseResourceKp)
    {
        return courseResourceKpMapper.insertCourseResourceKp(courseResourceKp);
    }

    /**
     * 批量新增课程资源与知识点关联
     *
     * @param resourceId 资源ID
     * @param kpIds 知识点ID数组
     * @return 结果
     */
    @Override
    @Transactional
    public int batchInsertCourseResourceKp(Long resourceId, Long[] kpIds)
    {
        if (kpIds == null || kpIds.length == 0)
        {
            return 0;
        }

        // 先删除旧的关联
        courseResourceKpMapper.deleteCourseResourceKpByResourceId(resourceId);

        // 批量插入新的关联
        List<CourseResourceKp> list = new ArrayList<>();
        for (Long kpId : kpIds)
        {
            CourseResourceKp crk = new CourseResourceKp();
            crk.setResourceId(resourceId);
            crk.setKpId(kpId);
            crk.setIsConfirmed(0); // 默认未确认
            list.add(crk);
        }

        return courseResourceKpMapper.batchInsertCourseResourceKp(list);
    }

    /**
     * 修改课程资源与知识点关联
     *
     * @param courseResourceKp 课程资源与知识点关联信息
     * @return 结果
     */
    @Override
    public int updateCourseResourceKp(CourseResourceKp courseResourceKp)
    {
        return courseResourceKpMapper.updateCourseResourceKp(courseResourceKp);
    }

    /**
     * 删除课程资源与知识点关联信息
     *
     * @param id 课程资源与知识点关联ID
     * @return 结果
     */
    @Override
    public int deleteCourseResourceKpById(Long id)
    {
        return courseResourceKpMapper.deleteCourseResourceKpById(id);
    }

    /**
     * 根据资源ID删除课程资源与知识点关联
     *
     * @param resourceId 资源ID
     * @return 结果
     */
    @Override
    public int deleteCourseResourceKpByResourceId(Long resourceId)
    {
        return courseResourceKpMapper.deleteCourseResourceKpByResourceId(resourceId);
    }

    /**
     * 批量删除课程资源与知识点关联信息
     *
     * @param ids 需要删除的课程资源与知识点关联ID
     * @return 结果
     */
    @Override
    public int deleteCourseResourceKpByIds(Long[] ids)
    {
        return courseResourceKpMapper.deleteCourseResourceKpByIds(ids);
    }
}
