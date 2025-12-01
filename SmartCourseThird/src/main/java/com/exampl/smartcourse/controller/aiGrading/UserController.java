package com.exampl.smartcourse.controller.aiGrading;

import com.exampl.smartcourse.common.PageResult;
import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.entity.aiGrading.User;
import com.exampl.smartcourse.service.aiGrading.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-grading")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    public PageResult list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "order", required = false) String order
    ) {
        return userService.list(page, pageSize, sortBy, order);
    }

    @GetMapping("/users/{id}")
    public Result<User> get(@PathVariable("id") Long id) {
        return userService.get(id);
    }

    @PostMapping("/users")
    public Result<User> create(@RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping("/users/{id}")
    public Result<User> update(@PathVariable("id") Long id, @RequestBody User user) {
        return userService.update(id, user);
    }

    @DeleteMapping("/users/{id}")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        return userService.delete(id);
    }
}