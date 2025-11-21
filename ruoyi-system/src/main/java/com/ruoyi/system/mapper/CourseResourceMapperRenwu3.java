package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.CourseResourceRenwu3;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CourseResourceMapperRenwu3
{
    public CourseResourceRenwu3 selectCourseResourceById(Long id);

    public List<CourseResourceRenwu3> selectCourseResourceList(CourseResourceRenwu3 courseResource);

    public int insertCourseResource(CourseResourceRenwu3 courseResource);

    public int updateCourseResource(CourseResourceRenwu3 courseResource);

    public int deleteCourseResourceById(Long id);

    public int deleteCourseResourceByIds(Long[] ids);
}
