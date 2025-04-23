package com.creaturelove.sociallikebackend.controller;

import com.creaturelove.sociallikebackend.common.BaseResponse;
import com.creaturelove.sociallikebackend.common.ResultUtils;
import com.creaturelove.sociallikebackend.model.dto.DoThumbRequest;
import com.creaturelove.sociallikebackend.service.ThumbService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/thumb")
public class ThumbController {

    private ThumbService thumbService;

    ThumbController(ThumbService thumbService) {
        this.thumbService = thumbService;
    }

    @PostMapping("/do")
    public BaseResponse<Boolean> doThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        Boolean success = thumbService.doThumb(doThumbRequest, request);
        return ResultUtils.success(success);
    }
}
