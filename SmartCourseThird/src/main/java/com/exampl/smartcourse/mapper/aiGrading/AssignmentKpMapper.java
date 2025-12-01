package com.exampl.smartcourse.mapper.aiGrading;

import com.exampl.smartcourse.entity.aiGrading.AssignmentKp;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AssignmentKpMapper {

    @Select("SELECT id, assignment_id AS assignmentId, kp_id AS kpId, sequence, is_required AS isRequired, create_time AS createTime FROM assignment_kp WHERE id = #{id}")
    AssignmentKp selectById(@Param("id") Long id);

    @Insert("INSERT INTO assignment_kp(assignment_id, kp_id, sequence, is_required, create_time) VALUES(#{assignmentId}, #{kpId}, #{sequence}, #{isRequired}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AssignmentKp ak);

    @Update("UPDATE assignment_kp SET assignment_id=#{assignmentId}, kp_id=#{kpId}, sequence=#{sequence}, is_required=#{isRequired}, create_time=#{createTime} WHERE id=#{id}")
    int update(AssignmentKp ak);

    @Delete("DELETE FROM assignment_kp WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT id, assignment_id AS assignmentId, kp_id AS kpId, sequence, is_required AS isRequired, create_time AS createTime FROM assignment_kp WHERE assignment_id = #{assignmentId}")
    List<AssignmentKp> selectByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Select("SELECT id, assignment_id AS assignmentId, kp_id AS kpId, sequence, is_required AS isRequired, create_time AS createTime FROM assignment_kp WHERE kp_id = #{kpId}")
    List<AssignmentKp> selectByKpId(@Param("kpId") Long kpId);
}