package com.creaturelove.sociallikebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creaturelove.sociallikebackend.model.entity.Blog;
import com.creaturelove.sociallikebackend.model.entity.Thumb;
import com.creaturelove.sociallikebackend.model.entity.User;
import com.creaturelove.sociallikebackend.model.vo.BlogVO;
import com.creaturelove.sociallikebackend.service.BlogService;
import com.creaturelove.sociallikebackend.mapper.BlogMapper;
import com.creaturelove.sociallikebackend.service.ThumbService;
import com.creaturelove.sociallikebackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author zhangrenren
* @description 针对表【blog】的数据库操作Service实现
* @createDate 2025-04-21 22:33:02
*/
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
    implements BlogService{

    private UserService userService;

    private ThumbService thumbService;

    BlogServiceImpl(UserService userService, @Lazy ThumbService thumbService) {
        this.userService = userService;
        this.thumbService = thumbService;
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

        // find the thumb record by loginU
        // serId and blogId
        Thumb thumb = thumbService.lambdaQuery()
                .eq(Thumb::getUserId, loginUser.getId())
                .eq(Thumb::getBlogId, blog.getId())
                .one();

        blogVO.setHasThumb(thumb != null);

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
            // get blogIdSet
            Set<Long> blogIdSet = blogList.stream()
                    .map(Blog::getId)
                    .collect(Collectors.toSet());

            // get thumb List
            List<Thumb> thumbList = thumbService.lambdaQuery()
                    .eq(Thumb::getUserId, loginUser.getId())
                    .in(Thumb::getBlogId, blogIdSet)
                    .list();

            // iterate thumbList and put blogIdHasThumbMap
            thumbList.forEach(blogThumb -> blogIdHasThumbMap.put(blogThumb.getBlogId(), true));
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




