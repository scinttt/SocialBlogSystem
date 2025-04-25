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
    /**
     * thumb Lua script
     * KEYS[1]       -- thumb state key
     * ARGV[1]       -- blog ID
     * 返回:
     * -1: user already thumbed
     * 1: success
     */
    public static final RedisScript<Long> THUMB_SCRIPT_MQ = new DefaultRedisScript<>("""
                     local userThumbKey = KEYS[1]
                     local blogId = ARGV[1]
             
                     -- check thumbed
                     if redis.call("HEXISTS", userThumbKey, blogId) == 1 then
                         return -1
                     end
             
                     -- add thumb record
                     redis.call("HSET", userThumbKey, blogId, 1)
                     return 1
             """, Long.class);

    /**
     * undoThumb Lua Script
     * KEYS[1]
     * ARGV[1]
     * 返回:
     * -1: User Already thumbed
     * 1: Success
     */
    public static final RedisScript<Long> UNTHUMB_SCRIPT_MQ = new DefaultRedisScript<>("""
             local userThumbKey = KEYS[1]
             local blogId = ARGV[1]
             
             -- check already thumbed
             if redis.call("HEXISTS", userThumbKey, blogId) == 0 then
                 return -1
             end
             
             -- delete thumb record
             redis.call("HDEL", userThumbKey, blogId)
             return 1
             """, Long.class);
}
