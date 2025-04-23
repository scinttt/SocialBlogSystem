package com.creaturelove.sociallikebackend.service;

import com.creaturelove.sociallikebackend.model.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.creaturelove.sociallikebackend.model.vo.BlogVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author zhangrenren
*/
public interface BlogService extends IService<Blog> {
    BlogVO getBlogVOById(long blogId, HttpServletRequest request);

    List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request);
}
