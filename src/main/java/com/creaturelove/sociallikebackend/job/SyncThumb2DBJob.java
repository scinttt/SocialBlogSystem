package com.creaturelove.sociallikebackend.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creaturelove.sociallikebackend.mapper.BlogMapper;
import com.creaturelove.sociallikebackend.model.entity.Thumb;
import com.creaturelove.sociallikebackend.model.enums.ThumbTypeEnum;
import com.creaturelove.sociallikebackend.service.ThumbService;
import com.creaturelove.sociallikebackend.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SyncThumb2DBJob {
    private final ThumbService thumbService;

    private final BlogMapper blogMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedRate = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void run() {
        log.info("开始执行");
        DateTime nowDate = DateUtil.date();

        // if the second is 0-9, go back to last minute's 50 second
        int second = (DateUtil.second(nowDate) / 10 - 1) * 10;
        if(second == -10){
            second = 50;
            // go back to last minute
            nowDate = DateUtil.offsetMinute(nowDate, -1);
        }

        log.info("Current second: {}", second);

        String date = DateUtil.format(nowDate, "HH:mm:") + second;
        log.info("Synchronization time slice: {}", date);

        syncThumb2DBByDate(date);

        log.info("Synchronization completed");
    }


    @Async
    public void syncThumb2DBByDate(String date){
        // get temp thumb and cancel thumb data
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(date);
        log.info("Temp thumb key: {}", tempThumbKey);
        Map<Object, Object> allTempThumbMap = redisTemplate.opsForHash().entries(tempThumbKey);
        boolean thumbMapEmpty = CollUtil.isEmpty(allTempThumbMap);

        // synchronize thumb to database
        // build insert list and collect blogId
        Map<Long, Long> blogThumbCountMap = new HashMap<>();
        if(thumbMapEmpty){
            return;
        }

        log.info("Temp thumb data is not null");

        ArrayList<Thumb> thumbList = new ArrayList<>();
        LambdaQueryWrapper<Thumb> wrapper = new LambdaQueryWrapper<>();
        boolean needRemove = false;
        for(Object userIdBlogIdObj : allTempThumbMap.keySet()){
            String userIdBlogId = (String) userIdBlogIdObj;
            String[] userIdAndBlogId = userIdBlogId.split(StrPool.COLON);
            Long userId = Long.valueOf(userIdAndBlogId[0]);
            Long blogId = Long.valueOf(userIdAndBlogId[1]);
            // -1 cancel thumb, 1 thumb
            Integer thumbType = Integer.valueOf(allTempThumbMap.get(userIdBlogId).toString());
            if(thumbType == ThumbTypeEnum.INCR.getValue()){
                Thumb thumb = new Thumb();
                thumb.setUserId(userId);
                thumb.setBlogId(blogId);
                thumbList.add(thumb);
            }else if(thumbType == ThumbTypeEnum.DECR.getValue()) {
                // connect search condition, batch delete
                needRemove = true;
                wrapper
                        .or()
                        .eq(Thumb::getUserId, userId)
                        .eq(Thumb::getBlogId, blogId);
            }else {
                if (thumbType != ThumbTypeEnum.NON.getValue()) {
                    log.warn("Data Exception: {}", userId + "," + blogId + "," + thumbType);
                }
                continue;
            }
            // calculate thumb count
            blogThumbCountMap.put(blogId, blogThumbCountMap.getOrDefault(blogId, 0L) + thumbType);
        }
        // batch insert
        thumbService.saveBatch(thumbList);

        // batch delete
        if(needRemove){
            thumbService.remove(wrapper);
        }

        // batch update blog thumb count
        if(!blogThumbCountMap.isEmpty()){
            blogMapper.batchUpdateThumbCount(blogThumbCountMap);
        }

        // Asynchronously delete temp thumb data
        Thread.startVirtualThread(() -> redisTemplate.delete(tempThumbKey)
        );
    }

}
