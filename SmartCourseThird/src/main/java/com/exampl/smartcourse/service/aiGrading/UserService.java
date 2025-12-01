package com.exampl.smartcourse.service.aiGrading;

import com.exampl.smartcourse.common.PageResult;
import com.exampl.smartcourse.common.Result;
import com.exampl.smartcourse.entity.aiGrading.User;
import com.exampl.smartcourse.mapper.aiGrading.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public PageResult list(Integer page, Integer pageSize, String sortBy, String order) {
        int p = page != null && page > 0 ? page : 1;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;
        String ord = order != null && order.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        String sort = sortBy != null ? sortBy : "createTime";
        String column = mapSortColumn(sort);
        String orderClause = column + " " + ord;
        int offset = (p - 1) * ps;
        long total = userMapper.countAll();
        List<User> data = userMapper.selectPaged(orderClause, offset, ps);
        return new PageResult(200, "success", total, data);
    }

    public Result<User> get(Long id) {
        User u = userMapper.selectById(id);
        return new Result<>(200, "success", u);
    }

    public Result<User> create(User user) {
        userMapper.insert(user);
        return new Result<>(200, "success", user);
    }

    public Result<User> update(Long id, User user) {
        user.setId(id);
        userMapper.update(user);
        return new Result<>(200, "success", userMapper.selectById(id));
    }

    public Result<Boolean> delete(Long id) {
        int r = userMapper.deleteById(id);
        return new Result<>(200, "success", r > 0);
    }

    private String mapSortColumn(String sort) {
        switch (sort) {
            case "id": return "id";
            case "username": return "username";
            case "email": return "email";
            case "realName": return "real_name";
            case "role": return "role";
            case "status": return "status";
            case "updateTime": return "update_time";
            case "createTime":
            default: return "create_time";
        }
    }
}