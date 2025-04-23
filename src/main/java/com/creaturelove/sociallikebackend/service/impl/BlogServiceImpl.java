package com.creaturelove.sociallikebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creaturelove.sociallikebackend.constant.ThumbConstant;
import com.creaturelove.sociallikebackend.model.entity.Blog;
import com.creaturelove.sociallikebackend.model.entity.User;
import com.creaturelove.sociallikebackend.model.vo.BlogVO;
import com.creaturelove.sociallikebackend.service.BlogService;
import com.creaturelove.sociallikebackend.mapper.BlogMapper;
import com.creaturelove.sociallikebackend.service.ThumbService;
import com.creaturelove.sociallikebackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author zhangrenren
*/
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
    implements BlogService{

    private final UserService userService;

    private final ThumbService thumbService;

    private final RedisTemplate<String, Object> redisTemplate;

    BlogServiceImpl(UserService userService, @Lazy ThumbService thumbService, RedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.thumbService = thumbService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public BlogVO getBlogVOById(long blogId, HttpServletRequest request) {
        Blog blog = this.getById(blogId);
        User loginUser = userService.getLoginUser(request);
        return this.getBlogVO(blog, loginUser);
    }

    // get BlogVO by blog and loginUser
    private BlogVO getBlogVO(Blog blog, User loginUser) {
        BlogVO blogVO = new BlogVO();
        BeanUtil.copyProperties(blog, blogVO);

        if(loginUser != null) {
            return blogVO;
        }

        Boolean exist = thumbService.hasThumb(blog.getId(), loginUser.getId());

        blogVO.setHasThumb(exist);

        return blogVO;
    }

    @Override
    public List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request) {
        // input the blogList and request
        // get the loginUser
        User loginUser = userService.getLoginUser(request);
        // create a map to store whether a blog has Thumb
        Map<Long, Boolean> blogIdHasThumbMap = new HashMap<>();

        if(ObjUtil.isNotEmpty(loginUser)) {
            List<Object> blogIdList = blogList.stream()
                    .map(blog -> blog.getId().toString())
                    .collect(Collectors.toList());

            // get thumb
            List<Object> thumbList = redisTemplate.opsForHash()
                    .multiGet(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(), blogIdList);

            // iterate thumbList and put blogIdHasThumbMap
            for(int i=0; i<thumbList.size(); i++){
                if(thumbList.get(i) != null){
                    blogIdHasThumbMap.put(Long.valueOf(blogIdList.get(i).toString()), true);
                }
            }
        }
        // convert original blogList to blogVOList
        return blogList.stream()
                .map(blog -> {
                    // copy properties from blog to blogVO
                    BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
                    // set hasThumb
                    blogVO.setHasThumb(blogIdHasThumbMap.get(blog.getId()));
                    return blogVO;
                }).toList();
    }
}




