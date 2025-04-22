package com.creaturelove.sociallikebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creaturelove.sociallikebackend.model.entity.Blog;
import com.creaturelove.sociallikebackend.service.BlogService;
import com.creaturelove.sociallikebackend.mapper.BlogMapper;
import org.springframework.stereotype.Service;

/**
* @author zhangrenren
* @description 针对表【blog】的数据库操作Service实现
* @createDate 2025-04-21 22:33:02
*/
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
    implements BlogService{

}




