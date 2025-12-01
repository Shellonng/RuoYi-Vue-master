package com.exampl.smartcourse.dto.auth;

import lombok.Data;
import java.util.List;

@Data
public class LoginResponse {
    private UserProfile user;
    private List<CourseBrief> courses;
}
