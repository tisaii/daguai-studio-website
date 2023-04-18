--- 清空Redis中Sorted Set的空缓存
---如果该key对应的ZSET是空缓存 zset元素数量为1且含有score=-1的元素, 将zset清空, 清空返回true,没有操作返回false
local key = KEYS[1];
-- 判断key对应的zset是否有score=-1的元素
if (redis.call("zcount", key, -1, -1) > 0) then
    --2.如果含有score=-1的元素,代表其是空缓存
    --2.1将空缓存键删除
    redis.call("del", key)
    return true;
end
if (redis.call("zcount", key, 0, 2524579200) == 0) then
    --无此key,则返回true代表修改zset操作时不需对zset操作
    return true;
end
--3.否则返回false代表zset中存在数据且不是空缓存
return false;
