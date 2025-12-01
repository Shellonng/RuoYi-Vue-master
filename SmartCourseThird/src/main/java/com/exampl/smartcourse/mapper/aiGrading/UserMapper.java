package com.exampl.smartcourse.mapper.aiGrading;

import com.exampl.smartcourse.entity.aiGrading.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, password, email, real_name AS realName, avatar, role, status, create_time AS createTime, update_time AS updateTime FROM `user` WHERE id = #{id}")
    User selectById(@Param("id") Long id);

    @Select("SELECT id, username, password, email, real_name AS realName, avatar, role, status, create_time AS createTime, update_time AS updateTime FROM `user` WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    @Insert("INSERT INTO `user`(username, password, email, real_name, avatar, role, status, create_time, update_time) VALUES(#{username}, #{password}, #{email}, #{realName}, #{avatar}, #{role}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE `user` SET username=#{username}, password=#{password}, email=#{email}, real_name=#{realName}, avatar=#{avatar}, role=#{role}, status=#{status}, create_time=#{createTime}, update_time=#{updateTime} WHERE id=#{id}")
    int update(User user);

    @Delete("DELETE FROM `user` WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM `user`")
    long countAll();

    @Select("<script>SELECT id, username, password, email, real_name AS realName, avatar, role, status, create_time AS createTime, update_time AS updateTime FROM `user` ORDER BY ${orderClause} LIMIT #{limit} OFFSET #{offset}</script>")
    List<User> selectPaged(@Param("orderClause") String orderClause, @Param("offset") int offset, @Param("limit") int limit);
}