package com.tisai.daguai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tisai.daguai.common.CustomException;
import com.tisai.daguai.common.SystemException;
import com.tisai.daguai.domain.Category;
import com.tisai.daguai.dto.RedisKey;
import com.tisai.daguai.dto.Result;
import com.tisai.daguai.mapper.CategoryMapper;
import com.tisai.daguai.service.CategoryService;
import com.tisai.daguai.utils.RedisCacheUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tisai.daguai.utils.RedisContents.*;
import static java.lang.Math.min;

/**
 *
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{
    @Resource
    private RedisCacheUtils redisCacheUtils;

    private static final String CATEGORY_PAGE_IDS_KEY = CACHE_PAGE_CATEGORIES_PREFIX+CACHE_ALL_CATEGOTY_IDS;

    @Override
    public Result pageInfoWithCache(int page, int pageSize) {
        //根据Redis中List结构来存储需要分页的信息和分页查询
        //lpush存放元素
        //lrange分页查询
        //lrem + lpush更新元素

        //1.创建page对象
        var categoryPage = new Page<Category>(page,pageSize,0);
        List<String> list = getCategoryIDListWithRedis();

        //根据key获取元素个数
        var total = redisCacheUtils.countList(CATEGORY_PAGE_IDS_KEY);
        if (total == null || total == 0) {
            //无值,返回total=0的对象
            return Result.ok(categoryPage);
        }
        //有值,设置total
        categoryPage.setTotal(total);
        //根据id分页查询对应分类信息及跳过前n个元素实现分页效果
        var categories = list.stream().skip((page - 1) * pageSize).map(item ->
                getCategoryById(Long.valueOf(item))
        ).collect(Collectors.toList());
        categoryPage.setRecords(categories.subList(0, min(pageSize,categories.size())));
        return Result.ok(categoryPage);
    }

    /**
     * 在Redis中查询Category所有数据列表(如果没有会执行数据库查询并写入缓存)
     * 结果是按照更新时间倒序排序的CategoryId列表
     * @return  List<categoryId>
     */
    private List<String> getCategoryIDListWithRedis() {
        //1.创建条件
        var queryWrapper = new LambdaQueryWrapper<Category>().select(Category::getId).orderByDesc(Category::getUpdateTime);
        //2.构建Listkey
        var redisKey = RedisKey.create(CACHE_ALL_CATEGOTY_IDS, CACHE_PAGE_CATEGORIES_PREFIX, BLANK_STRING);
        //3.在Redis中查询Category所有数据列表(如果没有会执行数据库查询并写入缓存)
        return redisCacheUtils.avalancheAndPenetrationWithPage(redisKey,
                i ->
                        this.list(i).stream().map(Category::getId).collect(Collectors.toList())
                , queryWrapper, CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
    }

    /**
     * 根据id查询对应分类详细信息(包含Redis)
     * @param id id
     * @return Category信息
     */
    public Category getCategoryById(Long id) {
        return redisCacheUtils.avalancheAndPenetration(RedisKey.create(id,CACHE_CATEGORY_PREFIX,CACHE_CATEGORY_INFO_SUFFIX),Category.class,this::getById,CACHE_CATEGORY_TTL,TimeUnit.MINUTES);
    }

    /**
     * 添加category分类
     * @param category category
     * @return ok/fail
     */
    @Override
    public Result add(Category category) {
        if(category==null){
            throw new CustomException("非法输入");
        }
        //1.添加到数据库category表
        var success = save(category);
        if(!success){
            return Result.fail("添加失败");
        }
        //2.更新redis
        //3.更新redis cache:page:categories:ids List结构,将添加的id放到最上面(page中查询时使用的是倒序的id rightPushAll)
        //需判断空缓存,若是空缓存则需实现清空List
        redisCacheUtils.insertListElement(CATEGORY_PAGE_IDS_KEY, category.getId(), CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
        return Result.ok();
    }

    /**
     * 根据id更新category信息及缓存中的信息
     * @param category category
     * @return r
     */
    @Override
    public Result updateWithCacheById(Category category) {
        if(category==null){
            throw new CustomException("非法输入");
        }
        //1.更新数据库
        var success = updateById(category);
        if(!success){
            return Result.fail("更新失败");
        }
        //2.查询缓存中List是否有数据,先删除id对应的缓存
        //3.有数据,将该id放到最上面
        //4.无数据,无操作
        var id = category.getId().toString();
        var tKey = CACHE_CATEGORY_PREFIX + id + CACHE_CATEGORY_INFO_SUFFIX;
        var isUpdate = redisCacheUtils.updateListMoreElement(CATEGORY_PAGE_IDS_KEY,tKey, id, CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
        if(!isUpdate){
            throw new SystemException("Redis更新List操作服务异常");
        }
        return Result.ok("更新成功");
    }

    @Override
    public Result deleteById(Long id) {
        if(id==null){
            return Result.fail("非法输入");
        }
        //1.删除数据库中数据
        var success = removeById(id);
        if(!success){
            return Result.fail("删除失败");
        }
        //2.对缓存中List及元素对应缓存进行删除(如果可能的话)
        var tKey = CACHE_CATEGORY_PREFIX + id + CACHE_CATEGORY_INFO_SUFFIX;
        var isDelete = redisCacheUtils.deleteListMoreElement(CATEGORY_PAGE_IDS_KEY, tKey, id, CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
        if(!isDelete){
            throw new SystemException("Redis删除List操作服务异常");
        }
        //3.返回
        return Result.ok("删除成功");
    }

    @Override
    public Result listByType(int type) {
        if(type!=1&&type!=2){
            return Result.fail("非法输入");
        }
        //1.先在Redis中查询所有的categoryId
        var categoryIDList = getCategoryIDListWithRedis();
        //2.再根据categoryId获取过滤指定type的category对象
        var categoryList = categoryIDList.stream().map(
                i -> redisCacheUtils.avalancheAndPenetration(
                        RedisKey.create(i, CACHE_CATEGORY_PREFIX, CACHE_CATEGORY_INFO_SUFFIX),
                        Category.class,
                        this::getById,
                        CACHE_CATEGORY_TTL,
                        TimeUnit.MINUTES
                )
        ).filter(r -> r.getType() == type)
                .collect(Collectors.toList());
        //3.返回
        return Result.ok(categoryList);
    }
}




