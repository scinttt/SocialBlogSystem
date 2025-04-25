package com.creaturelove.sociallikebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creaturelove.sociallikebackend.constant.ThumbConstant;
import com.creaturelove.sociallikebackend.manager.cache.CacheManager;
import com.creaturelove.sociallikebackend.model.dto.DoThumbRequest;
import com.creaturelove.sociallikebackend.model.entity.Blog;
import com.creaturelove.sociallikebackend.model.entity.User;
import com.creaturelove.sociallikebackend.service.BlogService;
import com.creaturelove.sociallikebackend.service.ThumbService;
import com.creaturelove.sociallikebackend.model.entity.Thumb;
import com.creaturelove.sociallikebackend.mapper.ThumbMapper;
import com.creaturelove.sociallikebackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
* @author zhangrenren
*/
@Service
@Slf4j
@Primary
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
    implements ThumbService {

    private final UserService userService;

    private final BlogService blogService;

    private final TransactionTemplate transactionTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    private final CacheManager cacheManager;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request){
        if(doThumbRequest == null || doThumbRequest.getBlogId() == null){
            throw new RuntimeException("Request parameters wrong");
        }

        // get login user
        User loginUser = userService.getLoginUser(request);

        // lock the login user
        synchronized (loginUser.getId().toString().intern()){
            // functional transaction
            return transactionTemplate.execute(status -> {
                // get blogId
                Long blogId = doThumbRequest.getBlogId();

                // check if the blog exists from redis cache
                boolean exists = this.hasThumb(blogId, loginUser.getId());
                if(exists){
                    throw new RuntimeException("You have already liked this blog");
                }

                // update the thumb count of the blog
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount + 1")
                        .update();

                // create a new thumb record
                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);

                // check if update success
                boolean success = update && this.save(thumb);

                // store thumb record into redis
                if(success) {
                    String hashKey = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId();
                    String fieldKey = blogId.toString();
                    Long realThumbId = thumb.getId();

                    redisTemplate.opsForHash().put(hashKey, fieldKey, realThumbId);

                    cacheManager.putIfPresent(hashKey, fieldKey, realThumbId);
                }

                // execute after successful update
                return success;
            });
        }
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("Request parameters wrong");
        }

        // get login user
        User loginUser = userService.getLoginUser(request);

        // lock the login user
        synchronized (loginUser.getId().toString().intern()) {
            Long blogId = doThumbRequest.getBlogId();
            Object thumbIdObj = redisTemplate.opsForHash()
                    .get(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(), blogId.toString());

            if (thumbIdObj == null) {
                throw new RuntimeException("You have not liked this blog");
            }

            Long thumbId = Long.valueOf(thumbIdObj.toString());

            boolean update = blogService.lambdaUpdate()
                    .eq(Blog::getId, blogId)
                    .setSql("thumbCount = thumbCount - 1")
                    .update();

            boolean success = update && this.removeById(thumbId);

            // update cache after update successfully update database
            if(success) {
                String hashKey = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId();
                String fieldKey = blogId.toString();
                redisTemplate.opsForHash().delete(hashKey, fieldKey);
                cacheManager.putIfPresent(hashKey, fieldKey, ThumbConstant.UN_THUMB_CONSTANT);
            }

            return success;
        }
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        Object thumbIdObj = cacheManager.get(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());

        if(thumbIdObj == null){
            return false;
        }

        Long thumbId = (Long) thumbIdObj;

        return !thumbId.equals(ThumbConstant.UN_THUMB_CONSTANT);
    }
}




