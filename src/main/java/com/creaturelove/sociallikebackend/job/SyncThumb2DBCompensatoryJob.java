package com.creaturelove.sociallikebackend.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.creaturelove.sociallikebackend.constant.ThumbConstant;
import com.creaturelove.sociallikebackend.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/*
* This compensate job will check if there is unsynchronized temp thumb data in redis cache
* Then it will synchronize every time slice of data to database by calling syncThumb2DBJob method
* */

@Component
@Slf4j
@RequiredArgsConstructor
public class SyncThumb2DBCompensatoryJob {

    private final RedisTemplate<String, Object> redisTemplate;

    private final SyncThumb2DBJob syncThumb2DBJob;

    @Scheduled(cron = "0 0 2 * * *")
    public void run(){
        log.info("Start compensating data");

        Set<String> thumbKeys = redisTemplate.keys(RedisKeyUtil.getTempThumbKey("") + "*");
        Set<String> needHandleDataSet = new HashSet<>();
        thumbKeys.stream().filter(ObjUtil::isNotNull).forEach(thumbKey ->
                needHandleDataSet.add(
                        thumbKey.replace(
                                ThumbConstant.TEMP_THUMB_KEY_PREFIX.formatted(""), ""
                        )
                )
        );

        if(CollUtil.isEmpty(needHandleDataSet)){
            log.info("No data to compensate");
            return;
        }

        // compensate data
        for(String date : needHandleDataSet){
            log.info("[SyncThumb2DBJob] 调度触发——timeSlice={}", date);
            syncThumb2DBJob.syncThumb2DBByDate(date);
        }

        log.info("Compensation completed");
    }

}
