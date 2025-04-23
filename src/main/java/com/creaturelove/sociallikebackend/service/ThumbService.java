package com.creaturelove.sociallikebackend.service;

import com.creaturelove.sociallikebackend.model.dto.DoThumbRequest;
import com.creaturelove.sociallikebackend.model.entity.Thumb;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author zhangrenren
* @description 针对表【thumb】的数据库操作Service
* @createDate 2025-04-21 22:33:02
*/
public interface ThumbService extends IService<Thumb> {
    Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);

}
