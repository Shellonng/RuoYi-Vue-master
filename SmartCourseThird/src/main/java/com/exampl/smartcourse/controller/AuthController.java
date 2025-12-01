package com.exampl.smartcourse.controller;

import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.dto.auth.LoginRequest;
import com.exampl.smartcourse.dto.auth.LoginResponse;
import com.exampl.smartcourse.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
