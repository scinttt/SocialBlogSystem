package com.creaturelove.sociallikebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creaturelove.sociallikebackend.constant.UserConstant;
import com.creaturelove.sociallikebackend.model.entity.User;
import com.creaturelove.sociallikebackend.service.UserService;
import com.creaturelove.sociallikebackend.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
* @author zhangrenren
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-04-21 22:33:02
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(UserConstant.LOGIN_USER);
    }
}




