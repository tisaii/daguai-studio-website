--- 清空Redis中列表的空缓存, 如果key对应的List是空缓存,0号是"",将List清空,执行清空操作则返回true,没有操作则返回false
local key = KEYS[1];
--1.判断List的大小
local size = redis.call("llen",key);
--2.如果大小为1且第一个元素值为"",代表其是空缓存
if(size==1 and (redis.call("lindex",key,0)=="")) then
    --2.1将空缓存pop出,返回true
    redis.call("rpop",key)
    return true;
else
    --3.否则返回false
    return false;
end
