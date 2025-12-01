package com.exampl.smartcourse.service;

import com.exampl.smartcourse.common.ErrorCode;
import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.dto.auth.CourseBrief;
import com.exampl.smartcourse.dto.auth.LoginRequest;
import com.exampl.smartcourse.dto.auth.LoginResponse;
import com.exampl.smartcourse.dto.auth.UserProfile;
import com.exampl.smartcourse.entity.Course;
import com.exampl.smartcourse.entity.aiGrading.User;
import com.exampl.smartcourse.mapper.CourseMapper;
import com.exampl.smartcourse.mapper.aiGrading.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;

    public Result<LoginResponse> login(LoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            return Result.failure(ErrorCode.INVALID_PARAM, "用户名或密码不能为空");
        }

        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null || !request.getPassword().equals(user.getPassword())) {
            return Result.failure(ErrorCode.INVALID_PARAM, "用户名或密码错误");
        }

        List<Course> courses = loadCoursesForUser(user);
        LoginResponse resp = new LoginResponse();
        resp.setUser(toProfile(user));
        resp.setCourses(courses.stream().map(this::toBrief).collect(Collectors.toList()));
        return Result.success("登录成功", resp);
    }

    private List<Course> loadCoursesForUser(User user) {
        if (user == null || !StringUtils.hasText(user.getRole())) {
            return Collections.emptyList();
        }
        String role = user.getRole().toUpperCase(Locale.ROOT);
        if ("TEACHER".equals(role)) {
            return courseMapper.selectByTeacher(user.getId());
        }
        if ("STUDENT".equals(role)) {
            return courseMapper.selectByStudent(user.getId());
        }
        return Collections.emptyList();
    }

    private UserProfile toProfile(User user) {
        UserProfile profile = new UserProfile();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setRealName(user.getRealName());
        profile.setEmail(user.getEmail());
        profile.setAvatar(user.getAvatar());
        profile.setRole(user.getRole());
        return profile;
    }

    private CourseBrief toBrief(Course c) {
        CourseBrief brief = new CourseBrief();
        brief.setId(c.getId());
        brief.setTitle(c.getTitle());
        brief.setDescription(c.getDescription());
        brief.setTerm(c.getTerm());
        brief.setStatus(c.getStatus());
        brief.setTeacherUserId(c.getTeacherUserId());
        brief.setStudentCount(c.getStudentCount());
        return brief;
    }
}
