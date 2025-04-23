package com.creaturelove.sociallikebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
* @author zhangrenren
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
    implements ThumbService {

    private final UserService userService;

    private final BlogService blogService;

    private final TransactionTemplate transactionTemplate;

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
                // check if the blog exists
                boolean exists = this.lambdaQuery()
                        .eq(Thumb::getUserId, loginUser.getId())
                        .eq(Thumb::getBlogId, blogId)
                        .exists();
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

                // execute only after update success
                return update && this.save(thumb);
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
            Thumb thumb = this.lambdaQuery()
                    .eq(Thumb::getUserId, loginUser.getId())
                    .eq(Thumb::getBlogId, blogId)
                    .one();
            if (thumb == null) {
                throw new RuntimeException("You have not liked this blog");
            }

            boolean update = blogService.lambdaUpdate()
                    .eq(Blog::getId, blogId)
                    .setSql("thumbCount = thumbCount - 1")
                    .update();

            return update && this.removeById(thumb.getId());
        }
    }
}




