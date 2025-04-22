package com.creaturelove.sociallikebackend.service;

import com.creaturelove.sociallikebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author zhangrenren
* @description 针对表【user】的数据库操作Service
* @createDate 2025-04-21 22:33:02
*/
public interface UserService extends IService<User> {

    User getLoginUser(HttpServletRequest request);
}
