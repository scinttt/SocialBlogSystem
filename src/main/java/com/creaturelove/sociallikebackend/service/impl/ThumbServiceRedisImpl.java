package com.creaturelove.sociallikebackend.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creaturelove.sociallikebackend.constant.RedisLuaScriptConstant;
import com.creaturelove.sociallikebackend.mapper.ThumbMapper;
import com.creaturelove.sociallikebackend.model.dto.DoThumbRequest;
import com.creaturelove.sociallikebackend.model.entity.Thumb;
import com.creaturelove.sociallikebackend.model.entity.User;
import com.creaturelove.sociallikebackend.model.enums.LuaStatusEnum;
import com.creaturelove.sociallikebackend.service.ThumbService;
import com.creaturelove.sociallikebackend.service.UserService;
import com.creaturelove.sociallikebackend.util.RedisKeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
* @author zhangrenren
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceRedisImpl extends ServiceImpl<ThumbMapper, Thumb>
    implements ThumbService {

    private final UserService userService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request){
        if(doThumbRequest == null || doThumbRequest.getBlogId() == null){
            throw new RuntimeException("Request parameters wrong");
        }

        // get login user
        User loginUser = userService.getLoginUser(request);
        // get blogId
        Long blogId = doThumbRequest.getBlogId();

        String timeSlice = getTimeSlice();

        // Redis Key
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());

        // execute Lua script
        long result = redisTemplate.execute(
                RedisLuaScriptConstant.THUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogId
        );

        if(LuaStatusEnum.FAIL.getValue() == result){
            throw new RuntimeException("You have already liked this blog");
        }

        // update when success
        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("Request parameters wrong");
        }

        // get login user
        User loginUser = userService.getLoginUser(request);
        // get blogId
        Long blogId = doThumbRequest.getBlogId();

        String timeSlice = getTimeSlice();
        // Redis Key
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());

        // execute Lua script
        long result = redisTemplate.execute(
                RedisLuaScriptConstant.UN_THUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogId
        );

        // handle result based on return value
        if(result == LuaStatusEnum.FAIL.getValue()){
            throw new RuntimeException("You have not liked this blog");
        }

        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return redisTemplate
                .opsForHash()
                .hasKey(RedisKeyUtil.getUserThumbKey(userId), blogId.toString());
    }

    private String getTimeSlice(){
        DateTime nowDate = DateUtil.date();

        // retrieve the nearest second
        return DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10) * 10;
    }
}




