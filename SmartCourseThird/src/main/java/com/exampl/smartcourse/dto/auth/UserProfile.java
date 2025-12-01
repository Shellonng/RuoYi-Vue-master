package com.exampl.smartcourse.dto.auth;

import lombok.Data;

@Data
public class UserProfile {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String avatar;
    private String role;
}
