package com.creaturelove.sociallikebackend.service;

import com.creaturelove.sociallikebackend.model.dto.DoThumbRequest;
import com.creaturelove.sociallikebackend.model.entity.Thumb;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author zhangrenren
*/
public interface ThumbService extends IService<Thumb> {
    Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);

    Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);
}
