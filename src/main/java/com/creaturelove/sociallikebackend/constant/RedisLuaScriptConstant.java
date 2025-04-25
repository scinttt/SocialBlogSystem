package com.creaturelove.sociallikebackend.constant;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public class RedisLuaScriptConstant {
    /*
    * thumb lua script
    * KEYS[1] - temp thumb key
    * KEYS[2] - user thumb status key
    * ARGV[1] - user id
    * ARGV[2] - blog id
    * return:
    * 1 - success
    * -1 - fail
    */

    public static final RedisScript<Long> THUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]
            local userThumbKey = KEYS[2]
            local userId = ARGV[1]
            local blogId = ARGV[2]
            
            -- 1. check if the user has already liked the blog
            if redis.call('HEXISTS', userThumbKey, blogId) == 1 then
                return -1 -- user has already liked the blog
            end
            
            -- 2. get old value(Default: 0)
            local hashKey = userId .. ':' .. blogId
            local oldNumber = tonumber(redis.call('HGET', tempThumbKey, hashKey) or 0)
            
            -- 3. calculate new value
            local newNumber = oldNumber + 1
            
            -- 4. Atomic update: write temp count + marked user as thumbed
            redis.call('HSET', tempThumbKey, hashKey, newNumber)
            redis.call('HSET', userThumbKey, blogId, 1)
            
            return 1 -- success
            """, Long.class);

    public static final RedisScript<Long> UN_THUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]
            local userThumbKey = KEYS[2]
            local userId = ARGV[1]
            local blogId = ARGV[2]
            
            -- 1. check if the user has already liked the blog
            if redis.call('HEXISTS', userThumbKey, blogId) == 0 then
                return -1 -- user has not liked the blog
            end
            
            -- 2. get old value(Default: 0)
            local hashKey = userId .. ':' .. blogId
            local oldNumber = tonumber(redis.call('HGET', tempThumbKey, hashKey) or 0)
            
            -- 3. calculate new value
            local newNumber = oldNumber - 1
            
            -- 4. Atomic update: write temp count + marked user as un-thumbed
            redis.call('HSET', tempThumbKey, hashKey, newNumber)
            redis.call('HDEL', userThumbKey, blogId)
            
            return 1 -- success
            """, Long.class);

}
