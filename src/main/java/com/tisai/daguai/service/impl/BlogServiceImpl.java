package com.tisai.daguai.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tisai.daguai.common.CustomException;
import com.tisai.daguai.common.SystemException;
import com.tisai.daguai.domain.Blog;
import com.tisai.daguai.domain.BlogProduct;
import com.tisai.daguai.dto.BlogDto;
import com.tisai.daguai.dto.RedisKey;
import com.tisai.daguai.dto.Result;
import com.tisai.daguai.mapper.BlogMapper;
import com.tisai.daguai.service.BlogProductService;
import com.tisai.daguai.service.BlogService;
import com.tisai.daguai.service.CategoryService;
import com.tisai.daguai.utils.RedisCacheUtils;
import com.tisai.daguai.utils.RedisIDWorker;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tisai.daguai.utils.RedisContents.*;
import static com.tisai.daguai.utils.SystemContents.BATH_PATH;

/**
 *
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
        implements BlogService {

    @Resource
    private BlogProductService blogProductService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private RedisCacheUtils redisCacheUtils;
    @Resource
    private RedisIDWorker redisIDWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    //Blog 的ListKey
    private static final String BLOG_PAGE_IDS_KEY = CACHE_PAGE_BLOGS_PREFIX + CACHE_ALL_BLOG_IDS;

    @Override
    public Result pageBynameWithRedis(int page, int pageSize, String name, String categoryId) {
        //构造条件
        Page<BlogDto> blogDtoPage = new Page<>(page, pageSize, 0);
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .orderByDesc(Blog::getUpdateTime).select(Blog::getId);

        //按更新顺序查询blog 所有id
        var redisKey = RedisKey.create(CACHE_ALL_BLOG_IDS, CACHE_PAGE_BLOGS_PREFIX, BLANK_STRING);
        var list = redisCacheUtils.avalancheAndPenetrationWithPage(redisKey,
                i ->
                        this.list(i).stream().map(Blog::getId).collect(Collectors.toList())
                , queryWrapper, CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
        if (list == null || list.isEmpty()) {
            //无值,返回total=0的对象
            return Result.ok(blogDtoPage);
        }

        //根据id分页查询对应博客信息和匹配条件查询, 跳过前n个元素实现分页效果
        var blogDtoList = list.stream().skip((page - 1) * pageSize).map(item ->
                getDtoById(Long.valueOf(item))
        )
                //设置过滤条件,即实现条件查询
                .filter(i -> (//分类id为空或相等
                (StrUtil.isBlank(categoryId)||i.getCategoryId().toString().equals(categoryId))
                &&//内容为空或内容与标题或内容相等
                (StrUtil.isBlank(name)||i.getTitle().contains(name)||i.getContent().contains(name))
                )
        ).collect(Collectors.toList());
        if (blogDtoList.isEmpty()) {
            //无值,返回total=0的对象
            return Result.ok(blogDtoPage);
        }
        //有值,设置total
        blogDtoPage.setTotal(blogDtoList.size());
        //对分页后的dto查询并set 分类名称
        var r = blogDtoList.subList(0, Math.min(pageSize, blogDtoList.size()));
/*        r.forEach(
                i->i.setCategoryName(categoryService.getCategoryById(i.getCategoryId()).getName())
        );*/
        return Result.ok(blogDtoPage.setRecords(r));
    }


    /**
     * 根据id结合Redis查询Blog基本信息及关联产品信息
     *
     * @param id id
     * @return msg
     */
    @Override
    public Result getDtoByIdWithRedis(Long id) {
        //1.判断id是否为空
        if (id == null) {
            return Result.fail("id为空");
        }
        //调用getDtoById获得Dto对象并返回值
        return Result.ok(getDtoById(id));
    }

    /**
     * 根据id结合Redis查询Blog基本信息及关联产品信息
     *
     * @param id id
     * @return 查询后的blogDto
     */
    public BlogDto getDtoById(Long id) {

        //2.根据id查询基本博客信息(采用Redis)
        var blog = redisCacheUtils.avalancheAndPenetration(
                id,
                CACHE_BLOG_PREFIX,
                CACHE_BLOG_BASIC_INFO_SUFFIX,
                Blog.class, this::getById,
                CACHE_BLOG_BASIC_INFO_TTL,
                TimeUnit.MINUTES);
        //3.根据博客id查询BlogProduct表中的所有关联信息
        var list = redisCacheUtils.avalancheAndPenetration(
                id, CACHE_BLOG_PREFIX, CACHE_BLOG_WITH_PRODUCT_SUFFIX, List.class,
                i -> {
//                    return blogProductService.list(new LambdaQueryWrapper<BlogProduct>().eq(BlogProduct::getBlogId,i));
                    var queryWrapper = new LambdaQueryWrapper<BlogProduct>();
                    queryWrapper.eq(BlogProduct::getBlogId, i);
                    return blogProductService.list(queryWrapper);
                }, CACHE_BLOG_WITH_PRODUCT_TTL, TimeUnit.MINUTES
        );
        //4.设置dto对象
        var blogDto = new BlogDto();
        BeanUtil.copyProperties(blog, blogDto);

        blogDto.setCategoryName(categoryService.getCategoryById(blog.getCategoryId()).getName());
        //noinspection unchecked
        blogDto.setBlogProducts(list);
        return blogDto;
    }

    /**
     * 根据id更新博客内容及其包含产品信息
     *
     * @param blogDto 更新后的blog
     * @return msg
     */
    @Transactional
    @Override
    public Result updateDtoWithRedis(BlogDto blogDto) {
        //1.先更新blog表中的信息
        var blogId = blogDto.getId();
        updateById(blogDto);
        //2.再删除blogProduct表中的所有有关数据
        LambdaQueryWrapper<BlogProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BlogProduct::getBlogId, blogId);
        blogProductService.remove(queryWrapper);
        //3.再将新的数据插入到blogProduct表中
        blogDto.getBlogProducts().forEach(item -> {
            item.setBlogId(blogId);
            blogProductService.save(item);
        });
        //4.更新Redis中List数据结构
        var infoDel = redisCacheUtils.updateListMoreElement(BLOG_PAGE_IDS_KEY, CACHE_BLOG_PREFIX + blogId + CACHE_BLOG_BASIC_INFO_SUFFIX, blogId, CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
        //5.删除Redis中blog关联的产品缓存
        var proDel = stringRedisTemplate.delete(CACHE_BLOG_PREFIX + blogId + CACHE_BLOG_WITH_PRODUCT_SUFFIX);
        if (!infoDel|| BooleanUtil.isFalse(proDel)){
            throw new SystemException("Redis服务更新操作错误");
        }
        return Result.ok("更新成功");
    }

    /**
     * 根据博客id批量删除博客
     *
     * @param ids 要删除的ids
     * @return msg
     */
    @Transactional
    @Override
    public synchronized Result deleteDtoByIds(List<Long> ids) {
        if(0==ids.size()){
            throw new CustomException("数据为空");
        }
        //构造BlogProduct表blogId相等的删除条件
        LambdaQueryWrapper<BlogProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(BlogProduct::getBlogId, ids);
        //先根据博客ids删除blogProduct表中信息
        blogProductService.remove(queryWrapper);
        //再根据ids逐个删除博客表中信息
        ids.forEach(item -> {
            Blog one = this.getById(item);
            //删除blog表中数据
            this.removeById(item);
//            删除Redis中与该id相关的数据
            //构造redis该id对应的keyPattern 例如cache:blog:1234567*
            var delKeyPatthern = CACHE_BLOG_PREFIX + item;
            var delSuccess = redisCacheUtils.deleteListMoreElement(BLOG_PAGE_IDS_KEY, delKeyPatthern, item, CACHE_BLOG_TTL, TimeUnit.MINUTES);
            if(!delSuccess){
                throw new SystemException("Redis删除服务操作异常");
            }
            //当数据库和Redis中数据都成功删除时才删除对应的图片文件
            if (StringUtils.isNotEmpty(one.getImage())) {
                //有图片
                //file=../img/xxx.xxx
                //根据ids删除服务器中对应的图片
                File file = new File(BATH_PATH + one.getImage());
                try {
                    var fileDel = file.delete();
                    if(!fileDel){
                        //删除失败,可能是该文件在服务器上已经被删除
                        throw new SystemException("服务器文件 : "+file.getPath()+"未找到");
                    }
                } catch (SecurityException e) {
                    //Security异常,该文件不可删除
                    throw new SystemException("文件:"+file.getPath()+"Security检查失败,文件拒绝删除");
                }
            }
        });
        return Result.ok("删除成功");
    }


    /**
     * 添加博客及其关联产品
     *
     * @param blogDto blog及对应产品列表类
     */
    @Override
    @Transactional
    public Result addBlogDtoWithRedis(BlogDto blogDto) {
        if(blogDto==null){
            return Result.fail("非法输入");
        }
        //生成一个id
        var blogId = redisIDWorker.getId(BLOG_ID_BUSINESS);
        //设置该id到blog中
        blogDto.setId(blogId);
        //1.将基本信息加入到blog表中
        var isSave = save(blogDto);
        if(!isSave) throw new CustomException("添加失败");
        //2.将关联的产品信息全部加入到blog_product表中
        blogDto.getBlogProducts().forEach(item -> {
            BlogProduct blogProduct = new BlogProduct();
            blogProduct.setProductId(item.getProductId());
            blogProduct.setBlogId(blogId);
            blogProduct.setName(item.getName());
            //插入
            blogProductService.save(blogProduct);
        });
        //3.向Redis List结构中最上方添加此博客id
        var success = redisCacheUtils.insertListElement(BLOG_PAGE_IDS_KEY, blogId, CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
        if(!success) throw new SystemException("Redis服务List插入操作错误");
        //4.返回
        return Result.ok("更新成功");
    }
}




