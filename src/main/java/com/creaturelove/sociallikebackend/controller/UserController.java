package com.creaturelove.sociallikebackend.controller;

import com.creaturelove.sociallikebackend.common.BaseResponse;
import com.creaturelove.sociallikebackend.common.ResultUtils;
import com.creaturelove.sociallikebackend.constant.UserConstant;
import com.creaturelove.sociallikebackend.model.entity.User;
import com.creaturelove.sociallikebackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
public class UserController {

    private UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public BaseResponse<User> Login(long userId, HttpServletRequest request) {
        User user = userService.getById(userId);
        request.getSession().setAttribute(UserConstant.LOGIN_USER, user);
        return ResultUtils.success(user);
    }

    @GetMapping("/get/login")
    public BaseResponse<User> getLoginUser(HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(UserConstant.LOGIN_USER);
        return ResultUtils.success(loginUser);
    }

}
