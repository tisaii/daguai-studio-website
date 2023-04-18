package com.tisai.daguai.utils;

import com.tisai.daguai.common.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIDWorker {

    private final StringRedisTemplate stringRedisTemplate;
    public static final String ID_WORKER_PREFIX = "idWorker:";
    public static final int TIMESTAMP_MOV_BIT = 32;
    /**
     * 起始时间(2023-1-1:0:0:0)
     */
    private static final long BEGIN_TIMESTAMP = 1672531200L;

    @Autowired
    public RedisIDWorker(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;

    }

    //ID格式:时间戳+32位(位数可调整)
    public long getId(String businessName){
        //1.获取当前时间
        var now = LocalDateTime.now();
        var nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        var timeFormat = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd:"));
        //2.拼接key
        var key = ID_WORKER_PREFIX + timeFormat + businessName;
        //3.对指定key执行increase操作并得到+1后的值用来拼接ID
        var count = stringRedisTemplate.opsForValue().increment(key);
        if (count==null){
            throw new SystemException("Redis服务increase操作错误");
        }
        //4.拼接ID(时间戳左移)并返回
        return ((nowSecond-BEGIN_TIMESTAMP)<<TIMESTAMP_MOV_BIT)|count;
    }
}
