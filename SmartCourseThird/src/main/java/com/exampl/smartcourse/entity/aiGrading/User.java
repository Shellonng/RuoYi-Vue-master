package com.exampl.smartcourse.entity.aiGrading;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String realName;
    private String avatar;
    private String role;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}