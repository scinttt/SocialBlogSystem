package com.creaturelove.sociallikebackend.manager.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CacheManager {
    private TopK hotKeyDetector;

    private Cache<String, Object> localCache;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Bean
    public TopK getHotKeyDetector() {
        hotKeyDetector = new HeavyKeeper(
                // monitor top 100 hot keys
                100,
                // each bucket width is 100000
                100000,
                // each bucket depth is 5
                5,
                // decay factor is 0.92
                0.92,
                // minimum count is 10
                10
        );

        return hotKeyDetector;
    }

    @Bean
    public Cache<String, Object> localCache() {
        return localCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    // helper method: build cache key
    private String buildCacheKey(String hashKey, String key) {
        return hashKey + ":" + key;
    }

    public Object get(String hashKey, String key) {
        // build the unique cache key
        String compositeKey = buildCacheKey(hashKey, key);

        // 1. search local cache
        Object value = localCache.getIfPresent(compositeKey);
        if (value != null) {
            log.info("localcache get data {} = {}", compositeKey, value);
            // Record access Count（every time + 1）
            hotKeyDetector.add(key, 1);
            return value;
        }

        // 2. Local cache miss, search redis
        Object redisValue = redisTemplate.opsForHash().get(hashKey, key);
        if (redisValue == null) {
            return null;
        }

        // 3. Record Access（Count +1）
        AddResult addResult = hotKeyDetector.add(key, 1);

        // 4. if the key is in TopK, then cache it
        if (addResult.isHotKey()) {
            localCache.put(compositeKey, redisValue);
        }

        return redisValue;
    }

    public void putIfPresent(String hash, String key, Object value) {
        // build the unique cache key
        String compositeKey = buildCacheKey(hash, key);

        // 1. search local cache
        Object object = localCache.getIfPresent(compositeKey);
        if (object == null) {
            return;
        }

        localCache.put(compositeKey, value);
    }

    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.SECONDS)
    public void cleanHotKeys() {
        hotKeyDetector.fading();
    }

}

