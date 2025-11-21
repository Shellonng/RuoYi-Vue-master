package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.CourseResourceKpRenwu3;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CourseResourceKpMapperRenwu3
{
    public CourseResourceKpRenwu3 selectCourseResourceKpById(Long id);

    public List<CourseResourceKpRenwu3> selectCourseResourceKpList(CourseResourceKpRenwu3 courseResourceKp);

    public int insertCourseResourceKp(CourseResourceKpRenwu3 courseResourceKp);

    public int updateCourseResourceKp(CourseResourceKpRenwu3 courseResourceKp);

    public int deleteCourseResourceKpById(Long id);

    public int deleteCourseResourceKpByIds(Long[] ids);

    public int deleteCourseResourceKpByResourceId(Long resourceId);

    public List<Long> selectKpIdsByResourceId(Long resourceId);

    public int batchInsertCourseResourceKp(List<CourseResourceKpRenwu3> list);
}
