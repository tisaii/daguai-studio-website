package com.tisai.daguai.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.tisai.daguai.dto.PageDto;
import com.tisai.daguai.dto.RedisKey;
import com.tisai.daguai.dto.ZSETDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.tisai.daguai.utils.RedisContents.*;

/**
 * Redis解决缓存穿透和雪崩, 及缓存击穿问题 工具类
 */
@SuppressWarnings("ALL")
@Slf4j
@Component
public class RedisCacheUtils {


    private final StringRedisTemplate stringRedisTemplate;
    //清除List空缓存脚本
    private final DefaultRedisScript<Boolean> CLEAR_IFNULL_CACHE;
    //更新或删除 List元素与与其对应的多个cache元素的lua脚本
    private final DefaultRedisScript<Boolean> UPDATE_LIST_MORE_ELEMENT_SCRIPT;
    //清除ZSET空缓存脚本
    private final DefaultRedisScript<Boolean> CLEAR_IFNULL_CACHE_ZSET;
    //更新或删除 ZSET元素与与其对应的多个cache元素的lua脚本
    private final DefaultRedisScript<Boolean> UPDATE_LIST_MORE_ELEMENT_ZSET_SCRIPT;

    @Autowired
    public RedisCacheUtils(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;

        CLEAR_IFNULL_CACHE = new DefaultRedisScript<>();
        CLEAR_IFNULL_CACHE.setLocation(new ClassPathResource("lua/clearIfNullCache.lua"));
        CLEAR_IFNULL_CACHE.setResultType(Boolean.class);

        UPDATE_LIST_MORE_ELEMENT_SCRIPT = new DefaultRedisScript<>();
        UPDATE_LIST_MORE_ELEMENT_SCRIPT.setLocation(new ClassPathResource("lua/updateListMoreElement.lua"));
        UPDATE_LIST_MORE_ELEMENT_SCRIPT.setResultType(Boolean.class);

        CLEAR_IFNULL_CACHE_ZSET = new DefaultRedisScript<>();
        CLEAR_IFNULL_CACHE_ZSET.setLocation(new ClassPathResource("lua/clearIfNullCacheZSET.lua"));
        CLEAR_IFNULL_CACHE_ZSET.setResultType(Boolean.class);

        UPDATE_LIST_MORE_ELEMENT_ZSET_SCRIPT = new DefaultRedisScript<>();
        UPDATE_LIST_MORE_ELEMENT_ZSET_SCRIPT.setLocation(new ClassPathResource("lua/updateListMoreElementByZSET.lua"));
        UPDATE_LIST_MORE_ELEMENT_ZSET_SCRIPT.setResultType(Boolean.class);

    }

    /**
     * 添加Redis中String结构数据并设置ttl
     *
     * @param key        key
     * @param value      value
     * @param expireTime ttl
     * @param unit       单位
     */
    public void setExpireTime(String key, Object value, Long expireTime, TimeUnit unit) {
        //设置ttl
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), expireTime, unit);
    }


    /**
     * Redis解决查询内存穿透及雪崩问题
     *
     * @param key        RedisKey封装类
     * @param rClass     返回值类型
     * @param function   数据库降级处理
     * @param expireTime ttl
     * @param unit       时间单位
     * @param <R>        返回值泛型
     * @param <ID>       ID泛型
     * @return 查询对象
     */
    public <R, ID> R avalancheAndPenetration(RedisKey<ID> key, Class<R> rClass,
                                             Function<ID, R> function,
                                             Long expireTime, TimeUnit unit
    ) {
        return avalancheAndPenetration(key.getId(), key.getKeyPrefix(), key.getKeySuffix(), rClass, function, expireTime, unit);
    }


/*
    public <ID,R> R avalancheAndPenetrationIn(ID id, String keyPrefix, String keySuffix , Class<R> rClass,
                                           Function<ID, R> function,
                                           Long expireTime, TimeUnit unit){
        if(rClass==List.class){
            avalancheAndPenetrationList(id,keyPrefix,keySuffix,
                    function.andThen(i->(List<T>)i),
                    expireTime, unit);
        }
    }




    public <ID> List<T> avalancheAndPenetrationList(ID id, String keyPrefix, String keySuffix,
                                             Function<ID, List<T>> function,
                                             Long expireTime, TimeUnit unit
    ) {
        //1.先根据id和前缀组装key
        var key = keyPrefix + id.toString() + keySuffix;
        //加锁避免并发错误
        synchronized (key.intern()) {
            //2.根据key在Redis中查询对应数据
            var jsonStr = stringRedisTemplate.opsForValue().get(key);
            //3.判断是否有数据
            if (!StrUtil.isBlank(jsonStr)) {
                //4.有数据,肯定是集合,所以要调用JSONUtil里的toList
                    return JSONUtil.toList(jsonStr, elementType);
            }
            if (jsonStr != null) {
                //不为null即为空缓存, 空缓存取出的值为"", 返回null
                return null;
            }
            //5.无数据,在数据库中取值
            var r = function.apply(id);
            //判断r结果是否有值
            if (r == null||r.isEmpty()) {
                //r为空, 设置空缓存解决内存穿透, ttl为较小值
                setExpireTime(key, "", CACHE_BLANK_TTL + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT), TimeUnit.SECONDS);
            }
            //6.将数据库中的值写入缓存
            //设置过期时间,添加一个随机数来解决内存雪崩
            setExpireTime(key, r, unit.toSeconds(expireTime + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT)), TimeUnit.SECONDS);
            //7.返回值
            return r;
        }
    }*/


    /**
     * Redis解决查询内存穿透及雪崩问题
     *
     * @param id         key中的id
     * @param keyPrefix  key前缀
     * @param keySuffix  key后缀
     * @param rClass     返回值类型
     * @param function   数据库降级处理
     * @param expireTime ttl
     * @param unit       时间单位
     * @param <R>        返回值泛型
     * @param <ID>       ID泛型
     * @return 查询对象
     */
    public <R, ID> R avalancheAndPenetration(ID id, String keyPrefix, String keySuffix, Class<R> rClass,
                                             Function<ID, R> function,
                                             Long expireTime, TimeUnit unit
    ) {
        //1.先根据id和前缀组装key
        var key = keyPrefix + id.toString() + keySuffix;
        //加锁避免并发错误
        synchronized (key.intern()) {
            //2.根据key在Redis中查询对应数据
            var jsonStr = stringRedisTemplate.opsForValue().get(key);
            //3.判断是否有数据
            if (!StrUtil.isBlank(jsonStr)) {
                //4.有数据,判断返回类型是单个对象还是集合,直接返回
                if (rClass == List.class) {
                    //集合
                    return (R) JSONUtil.toList(jsonStr, Object.class);
                }
                //单个对象
                return JSONUtil.toBean(jsonStr, rClass);
            }
            if (jsonStr != null) {
                //不为null即为空缓存, 空缓存取出的值为"", 返回null
                return null;
            }
            //5.无数据,在数据库中取值
            var r = function.apply(id);
            //判断r结果是否有值
            if (r == null) {
                //r为null, 设置空缓存解决内存穿透, ttl为较小值
                setExpireTime(key, "", CACHE_BLANK_TTL + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT), TimeUnit.SECONDS);
            }
            //6.将数据库中的值写入缓存
            //设置过期时间,添加一个随机数来解决内存雪崩
            setExpireTime(key, r, unit.toSeconds(expireTime + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT)), TimeUnit.SECONDS);
            //7.返回值
            return r;
        }
    }


    /**
     * 根据QueryWrapper条件查询数据库中表对应的id值
     * 这里的id值应按照updateTime倒序排序,也就是设置QueryWrapper时务必要加上orderByDESC语句
     * 返回值是某一类下的所有数据id(String类型)
     * 此方法解决了缓存穿透和雪崩问题
     * <p>
     * 例如想要分页查询blog表下的blog信息,此函数会返回(并写入缓存)blog表中所有id数据(按照数据库查询顺序)
     *
     * @param key          RedisKey封装类,key
     * @param function     数据库降级操作,根据此函数对数据库数据查询并写入缓存
     * @param queryWrapper 查询条件
     * @param expireTime   过期时间
     * @param unit         单位
     * @param <KeyID>      缓存key中ID类型
     * @param <T>          要查询的数据类型
     * @param <R>          数据库降级操作返回类型,需要继承List类
     * @return 某一类下的所有数据id(String类型)
     */
    public <KeyID, T, R extends List<?>> List<String> avalancheAndPenetrationWithPage(RedisKey<KeyID> key,
                                                                                      Function<Wrapper<T>, R> function, Wrapper<T> queryWrapper,
                                                                                      Long expireTime, TimeUnit unit) {
        //1.获取key
        var keyKey = key.getKey();
        //设置锁
        synchronized (keyKey.intern()) {
            //2.根据key在List结构中查询是否存在该元素
            var list = stringRedisTemplate.opsForList().range(keyKey, 0, -1);
            //3.判断是否存在元素
            if (list != null && !list.isEmpty()) {
                //5.判断是否为空缓存 : 规定空缓存是List第一个元素为""
                if (list.get(0).equals("")) {
                    return List.of();
                } else {
                    //4.存在
                    //4.1查询并返回结果
                    return list;
                }
            }
            //7.不是空缓存,则在数据库中查询
            var r = function.apply(queryWrapper);
            //8.判断查询结果,若为空则存入空缓存,空值设置ttl较短
            if (r.isEmpty()) {
                stringRedisTemplate.opsForList().rightPush(keyKey, "");
                stringRedisTemplate.expire(keyKey, CACHE_BLANK_TTL + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT), TimeUnit.SECONDS);
                return new ArrayList<>();
            } else {
                var strings = r.stream().map(Object::toString).collect(Collectors.toList());
                //9.不为空,将查询到的值写入到Redis
                stringRedisTemplate.opsForList().rightPushAll(keyKey, strings);
                stringRedisTemplate.expire(keyKey, unit.toSeconds(expireTime) + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT), TimeUnit.SECONDS);
                //返回值
                return strings;
            }
        }

    }


    /**
     * filter and select the id list of Database Table by key name, create or update time.
     * the method is implemented with Redis Sorted Set Data Struction
     * <p>
     * --input
     * you must input a supplier that returns ZSETDto (pass writing in mapper xml),
     * which should be the database opration to ensure the cache date can be get;
     * and input the min timestamp and max timestamp, and if you wanna ignore them,
     * you can set the min timestamp 0 and max now;
     * the page and pageSize mean the concrate data you wanna select. like page 0 and pageSize 10;
     * the expireTime and unit mean the ttl of data cache you wanna set.
     * <p>
     * --return
     * this method will return a object of PageDto, which fields include page,pageSize,total and dataList.
     * there are 3 types of result.
     * 1.null : blank cache
     * 2.not null but empty dataList : not have eligible data
     * 3.not null and not empty dataList : eligible data
     *
     * @param key          redisKey
     * @param supplier     supplier
     * @param minTimestamp minTimestamp
     * @param maxTimestamp maxTimestamp
     * @param page         page
     * @param pageSize     pageSize
     * @param expireTime   expireTime
     * @param unit         unit
     * @param <KeyID>      KeyID
     * @return pageDto
     */
    public <KeyID> PageDto<String> avalancheAndPenetrationWithPageZSET(RedisKey<KeyID> key,
                                                                                             Supplier<List<ZSETDto>> supplier,
                                                                                             long minTimestamp, long maxTimestamp,
                                                                                             int page, int pageSize,
                                                                                             Long expireTime, TimeUnit unit) {
        //1.获取key
        var keyKey = key.getKey();
        //加锁
        synchronized (keyKey.intern()) {
            //2.根据key在Zset中按照score倒序查询score(时间戳)在范围内max - min之间
            var size = stringRedisTemplate.opsForZSet().size(keyKey);
            //3.判断是否存在数据
            if (size != null && size > 0) {
                //根据查询是否有score=-1的来判断是否是空缓存
                var blankCacheSet = stringRedisTemplate.opsForZSet().rangeByScore(keyKey, -1, -1);
                if (blankCacheSet == null || blankCacheSet.isEmpty()) {
                    //3.1存在数据且不是空缓存,返回指定结果zrevrangeByScore key max min
                    Set<String> stringSet = stringRedisTemplate.opsForZSet()
                            .rangeByScore(keyKey, minTimestamp, maxTimestamp, (page - 1) * pageSize, pageSize);

                    if (stringSet == null || stringSet.isEmpty()) {
                        return PageDto.empty();
                    }
/*                    var idTotalSet = stringRedisTemplate.opsForZSet()
                            .rangeByScore(keyKey, minTimestamp, maxTimestamp);
                    if(idTotalSet==null||idTotalSet.isEmpty()){
                        return PageDto.empty();
                    }
                    var selectTotalSize = idTotalSet.size();*/
                    return new PageDto<String>(
                            page, pageSize,
                            (long) stringSet.size())
                            .list(new ArrayList<>(stringSet)
                            );
                }
                //3.2是空缓存,返回null
                //空缓存定义 : zset中只有一个元素,memeber=""且score=-1
                return null;
            }
            //4.没有查到数据,则数据库降级操作,降级操作要包含ID和time
            var r = supplier.get();
            //返回结果是List<ZSETDto>的sql语句
            //5.判断结果是否为空
            if (r == null || r.isEmpty()) {
                //6.结果为空,则设置空缓存,值为"",score=-1
                stringRedisTemplate.opsForZSet().add(keyKey, "", -1);
                stringRedisTemplate.expire(keyKey, CACHE_BLANK_TTL + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT), TimeUnit.SECONDS);
                return PageDto.empty();
            }
            //7.否则,批量存储数据
            var typedTuples = r.stream()
                    .map(i ->
                            (ZSetOperations.TypedTuple<String>) new DefaultTypedTuple<>(
                                    i.getId(),
                                    (double) (i.getCreateTime() != null ? i.getCreateTime().toEpochSecond(ZoneOffset.UTC) : i.getUpdateTime().toEpochSecond(ZoneOffset.UTC))
                            )
                    )
                    .collect(Collectors.toSet());
            stringRedisTemplate.opsForZSet().add(keyKey, typedTuples);
            //设置ttl
            stringRedisTemplate.expire(keyKey, unit.toSeconds(expireTime) + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT), TimeUnit.SECONDS);
        }
        //递归
        return avalancheAndPenetrationWithPageZSET(key, supplier, maxTimestamp, minTimestamp, page, pageSize, expireTime, unit);
    }


    /**
     * filter and select the id list of Database Table by key name, create(ASC) or update(DESC) time.
     * the method is implemented with Redis Sorted Set Data Struction
     *
     * --input
     * you must input a supplier that returns ZSETDto (pass writing in mapper xml and must one of createTime
     * and updateTime is NOT NULL),
     * which should be the database opration to ensure the cache date can be get.
     * If the value of key contains "ut", means select data order by updateTime and descending.
     * else contains "ct", order by createTime and ascending;
     * and input the min timestamp and max timestamp, and if you wanna ignore them,
     * you can set the min timestamp 0 and max now;
     * the expireTime and unit mean the ttl of data cache you wanna set.
     *
     * --return
     * this method will return a List typed String of ID (you wanna select from database)
     * there are 3 types of result.
     * 1.null : blank cache
     * 2.not null but empty dataList : not have eligible data
     * 3.not null and not empty dataList : eligible data
     *
     * @param key          key
     * @param supplier     supplier
     * @param minTimestamp minTimestamp
     * @param maxTimestamp maxTimestamp
     * @param expireTime   expireTime
     * @param unit         unit
     * @return id list
     */
    public List<String> avalancheAndPenetrationWithPageZSET(String key,
                                                                       Supplier<List<ZSETDto>> supplier,
                                                                       long minTimestamp, long maxTimestamp,
                                                                       Long expireTime, TimeUnit unit) {
        //加锁
        synchronized (key.intern()) {
            //2.根据key在Zset中按照score倒序查询score(时间戳)在范围内max - min之间
            var size = stringRedisTemplate.opsForZSet().size(key);
            //3.判断是否存在数据
            if (size != null && size > 0) {
                //根据查询是否有score=-1的来判断是否是空缓存
                var blankCacheSet = stringRedisTemplate.opsForZSet().rangeByScore(key, -1, -1);
                if (blankCacheSet == null || blankCacheSet.isEmpty()) {
                    //3.1存在数据且不是空缓存,返回指定结果zrevrangeByScore key max min
                    //判断key查询是根据更新还是根据创建时间
                    var idTotalSet = (Set<String>) new TreeSet<String>();
                    if (key.contains(CT_SUFFIX)) {
                        //包含ct,即是根据createTime查询,此时应该正序查询以方便检索
                        idTotalSet = stringRedisTemplate.opsForZSet()
                                .rangeByScore(key, minTimestamp, maxTimestamp);
                    } else {
                        //包含ut,即是根据updateTime查询,此时应该倒序查询即最近更新的靠前以方便检索
                        idTotalSet = stringRedisTemplate.opsForZSet()
                                .reverseRangeByScore(key, minTimestamp, maxTimestamp);
                    }
                    if (idTotalSet == null || idTotalSet.isEmpty()) {
                        //无符合范围的id
                        return List.of();
                    }
                    return new ArrayList<>(idTotalSet);

                }
                //3.2是空缓存,返回null
                //空缓存定义 : zset中只有一个元素,memeber=""且score=-1
                return null;
            }
            //4.没有查到数据,则数据库降级操作,降级操作要包含ID和time
            var r = supplier.get();
            //返回结果是List<ZSETDto>的sql语句
            //5.判断结果是否为空
            if (r == null || r.isEmpty()) {
                //6.结果为空,则设置空缓存,值为"",score=-1
                stringRedisTemplate.opsForZSet().add(key, "", -1);
                stringRedisTemplate.expire(key, CACHE_BLANK_TTL + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT), TimeUnit.SECONDS);
                return List.of();
            }
            //7.否则,批量存储数据
            var typedTuples = r.stream()
                    .map(i ->
                            (ZSetOperations.TypedTuple<String>) new DefaultTypedTuple<>(
                                    i.getId(),
                                    (double) (i.getCreateTime() != null ? i.getCreateTime().toEpochSecond(ZoneOffset.UTC) : i.getUpdateTime().toEpochSecond(ZoneOffset.UTC))
                            )
                    )
                    .collect(Collectors.toSet());
            stringRedisTemplate.opsForZSet().add(key, typedTuples);
            //设置ttl
            stringRedisTemplate.expire(key, unit.toSeconds(expireTime) + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT), TimeUnit.SECONDS);
        }
        //递归
        return avalancheAndPenetrationWithPageZSET(key, supplier, minTimestamp, maxTimestamp, expireTime, unit);
    }


    /**
     * filter and select the id list of Database Table by key name, create(ASC) or update(DESC) time.
     * the method is implemented with Redis Sorted Set Data Struction
     *
     * --input
     * you must input a supplier that returns ZSETDto (pass writing in mapper xml and must one of createTime
     * and updateTime is NOT NULL),
     * which should be the database opration to ensure the cache date can be get.
     * If the value of key contains "ut", means select data order by updateTime and descending.
     * else contains "ct", order by createTime and ascending;
     * and input the min timestamp and max timestamp, and if you wanna ignore them,
     * you can set the min timestamp 0 and max now;
     * the expireTime and unit mean the ttl of data cache you wanna set.
     *
     * --return
     * this method will return a List typed String of ID (you wanna select from database)
     * there are 3 types of result.
     * 1.null : blank cache
     * 2.not null but empty dataList : not have eligible data
     * 3.not null and not empty dataList : eligible data
     *
     * @param key          key
     * @param supplier     supplier
     * @param minTimestamp minTimestamp
     * @param maxTimestamp maxTimestamp
     * @param expireTime   expireTime
     * @param unit         unit
     * @return id list
     */
    public <KeyID> List<String> avalancheAndPenetrationWithPageZSET(RedisKey<KeyID> key,
                                                                       Supplier<List<ZSETDto>> supplier,
                                                                       long minTimestamp, long maxTimestamp,
                                                                       Long expireTime, TimeUnit unit) {
        return avalancheAndPenetrationWithPageZSET(key.getKey(), supplier, minTimestamp, maxTimestamp, expireTime, unit);
    }


    /**
     * 统计Redis中key对应的List的size
     *
     * @param key key
     * @return List.size
     */
    public Long countList(String key) {
        var size = stringRedisTemplate.opsForList().size(key);
        var index = stringRedisTemplate.opsForList().index(key, 0);
        if (size == null || size == 0 || (size == 1 && index != null && index.equals(""))) {
            return 0L;
        }
        return stringRedisTemplate.opsForList().size(key);
    }

    /**
     * 清空Redis中列表的空缓存
     * 如果该key对应的List是空缓存 0号是"" , 将List清空, 清空返回true,没有操作返回false
     *
     * @param key key
     * @return true/false
     */
    public boolean clearIfNullCache(String key) {
        return BooleanUtil.isTrue(stringRedisTemplate.execute(CLEAR_IFNULL_CACHE, List.of(key), BLANK_STRING));
    }

    /**
     * 清空Redis中Sorted Set的空缓存
     * 如果该key对应的ZSET是空缓存 zset元素数量为1且含有score=-1的元素, 将zset清空, 清空返回true,没有操作返回false
     *
     * @param key key
     * @return true/false
     */
    public boolean clearIfNullCacheZSET(String key) {
        return BooleanUtil.isTrue(stringRedisTemplate.execute(CLEAR_IFNULL_CACHE_ZSET, List.of(key), BLANK_STRING));
    }

    /**
     * 在List最上方插入一个元素
     *
     * @param key        key
     * @param t          element
     * @param expireTime 过期时间
     * @param unit       时间单位
     * @param <T>        元素类型
     * @return true/false
     */
    public <T> boolean insertListElement(String key, T t, Long expireTime, TimeUnit unit) {
        synchronized (key.intern()) {
            clearIfNullCache(key);
            stringRedisTemplate.opsForList().leftPush(key, t.toString());
            return BooleanUtil.isTrue(stringRedisTemplate.expire(key, unit.toSeconds(expireTime) + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT), TimeUnit.SECONDS));
        }
    }

    /**
     * 删除位于List中的元素,操作会将List中元素id及Cache中符合delKeysPattern的元素删除
     *
     * @param listKey        List的key
     * @param delKeysPattern 要删除关联key的pattern,为String类型,要删除的key中必须都包括这一部分
     * @param t              元素t,一般情况下为单个数据对应的id
     * @param expireTime     过期时间
     * @param unit           时间单位
     * @param <T>            t类型
     * @return true/false
     */
    public <T> boolean deleteListMoreElement(String listKey, String delKeysPattern, T t, Long expireTime, TimeUnit unit) {
        //执行lua脚本
        synchronized (listKey.intern()) {
            return BooleanUtil.isTrue(stringRedisTemplate.execute(UPDATE_LIST_MORE_ELEMENT_SCRIPT, Collections.singletonList(listKey), clearIfNullCache(listKey) ? "yes" : "no", t.toString(), LUA_DELETE, "*" + delKeysPattern + "*", String.valueOf(unit.toSeconds(expireTime) + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT))));
        }
    }

    /**
     * 更新位于List中的元素,操作会将List中元素id放置于最上方,及将Cache中符合delKeysPattern的元素删除
     *
     * @param listKey        List的key
     * @param delKeysPattern 要删除关联key的pattern,为String类型,要删除的key中必须都包括这一部分
     * @param t              元素t,一般情况下为单个数据对应的id
     * @param expireTime     过期时间
     * @param unit           时间单位
     * @param <T>            t类型
     * @return true/false
     */
    public <T> boolean updateListMoreElement(String listKey, String delKeysPattern, T t, Long expireTime, TimeUnit unit) {
        //执行lua脚本
        synchronized (listKey.intern()) {
            return BooleanUtil.isTrue(stringRedisTemplate.execute(UPDATE_LIST_MORE_ELEMENT_SCRIPT, Collections.singletonList(listKey), clearIfNullCache(listKey) ? "yes" : "no", t.toString(), LUA_UPDATE, "*" + delKeysPattern + "*", String.valueOf(unit.toSeconds(expireTime) + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT))));
        }
    }

    /**
     * update zset data in redis when client excute update opreation,the function will judge the
     *
     * @param zsetKey        zset key
     * @param delKeysPattern data cache key patthern that you wanna delete
     * @param t              element, member in zset struction here, commonly ID
     * @param timeStamp      the max number when excute zcount key min max, should input now timestamp
     * @param expireTime     ttl
     * @param unit           ttl unit
     * @param <T>            element type, commonly string or long
     * @return true or false
     */
    public <T> boolean updateZSETMoreElement(String zsetKey, String delKeysPattern, T t,  long timeStamp, Long expireTime, TimeUnit unit) {
        return BooleanUtil.isTrue(stringRedisTemplate.execute(UPDATE_LIST_MORE_ELEMENT_ZSET_SCRIPT, Collections.singletonList(zsetKey), clearIfNullCacheZSET(zsetKey) ? "yes" : "no", t.toString(), LUA_UPDATE, "*" + delKeysPattern + "*", String.valueOf(unit.toSeconds(expireTime) + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT)), String.valueOf(timeStamp)));
    }

    /**
     * delete zset data in redis when client excute delete opreation
     *
     * @param zsetKey        zset key
     * @param delKeysPattern data cache key patthern that you wanna delete
     * @param t              element, member in zset struction here, commonly ID
     * @param expireTime     ttl
     * @param unit           ttl unit
     * @param <T>            element type, commonly string or long
     * @return true or false
     */
    public <T> boolean deleteZSETMoreElement(String zsetKey, String delKeysPattern, T t, Long expireTime, TimeUnit unit) {
        return BooleanUtil.isTrue(stringRedisTemplate.execute(UPDATE_LIST_MORE_ELEMENT_ZSET_SCRIPT, Collections.singletonList(zsetKey), clearIfNullCacheZSET(zsetKey) ? "yes" : "no", t.toString(), LUA_DELETE, "*" + delKeysPattern + "*", String.valueOf(unit.toSeconds(expireTime) + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT))));
    }

    /**
     * insert zset data in redis when client excute insert opreation
     *
     * @param zsetKey        zset key
     * @param t              element, member in zset struction here, commonly ID
     * @param expireTime     ttl
     * @param unit           ttl unit
     * @param <T>            element type, commonly string or long
     * @return true or false
     */
    public <T> boolean insertZSETMoreElement(String zsetKey, T t,long timestamp, Long expireTime, TimeUnit unit) {
        return BooleanUtil.isTrue(stringRedisTemplate.execute(UPDATE_LIST_MORE_ELEMENT_ZSET_SCRIPT, Collections.singletonList(zsetKey), clearIfNullCacheZSET(zsetKey) ? "yes" : "no", t.toString(), LUA_INSERT, "", String.valueOf(unit.toSeconds(expireTime) + RandomUtil.randomLong(CACHE_AVALANCHE_RANDOM_LIMIT)),String.valueOf(timestamp)));
    }
}
