package com.creaturelove.sociallikebackend.mapper;

import com.creaturelove.sociallikebackend.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author zhangrenren
* @description 针对表【user】的数据库操作Mapper
* @createDate 2025-04-21 22:33:02
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




