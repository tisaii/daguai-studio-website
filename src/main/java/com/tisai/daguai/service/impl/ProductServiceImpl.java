package com.tisai.daguai.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tisai.daguai.common.CustomException;
import com.tisai.daguai.common.SystemException;
import com.tisai.daguai.domain.Product;
import com.tisai.daguai.dto.ProductDto;
import com.tisai.daguai.dto.RedisKey;
import com.tisai.daguai.dto.Result;
import com.tisai.daguai.mapper.ProductMapper;
import com.tisai.daguai.service.CategoryService;
import com.tisai.daguai.service.ProductService;
import com.tisai.daguai.utils.RedisCacheUtils;
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
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
    implements ProductService{

    @Resource
    private CategoryService categoryService;
    @Resource
    private RedisCacheUtils redisCacheUtils;

    private static final String PRODUCT_PAGE_IDS_KEY = CACHE_PAGE_PRODUCTS_PREFIX + CACHE_ALL_PRODUCT_IDS;


    @Override
    public Result pageWithRedis(int page, int pageSize, String name, String type) {
        var productDtoPage = new Page<ProductDto>(page, pageSize, 0);
        List<Long> productIdList = getProductIdListWithRedis();
//        var total = redisCacheUtils.countList(PRODUCT_PAGE_IDS_KEY);
//        if(total==null||total ==0){
//            //数据为空
//            return Result.ok(productDtoPage);
//        }
//        productDtoPage.setTotal(total);
        //空缓存或数据库中无数据
        if(productIdList==null||productIdList.isEmpty()){
            return Result.ok(productDtoPage);
        }

        //根据id分页查询产品及其分类名和匹配条件查询, 跳过前n个元素实现分页效果
        var productDtoList = productIdList.stream().skip((page - 1) * pageSize).map(this::getProductDtoById
        )
                //设置过滤条件,即实现条件查询
                .filter(i -> (//分类id为空或相等
                                (StrUtil.isBlank(type)||i.getCategoryId().toString().equals(type))
                                        &&//产品名包含name
                                        (StrUtil.isBlank(name)||i.getName().contains(name))
                        )
                ).collect(Collectors.toList());
        if(productDtoList.isEmpty()){
            //无符合条件的产品
            return Result.ok(productDtoPage);
        }
        //设置total
        productDtoPage.setTotal(productDtoList.size());
        var r = productDtoList.subList(0, Math.min(pageSize, productDtoList.size()));
        return Result.ok(productDtoPage.setRecords(r));
    }

    @Override
    @Transactional
    public Result saveWithRedis(Product product) {
        if(product==null){
            throw new CustomException("非法输入");
        }
        var save = save(product);
        if(!save){
            throw new CustomException("添加失敗");
        }
        //将product的id加入到对应的List中顶部
        var isSuccess = redisCacheUtils.insertListElement(PRODUCT_PAGE_IDS_KEY, product.getId(), CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
        if (!isSuccess){
            throw new SystemException("Redis插入服务错误");
        }
        return Result.ok("添加成功");
    }

    @Override
    public Result getOneById(Long id) {
        if(id==null){
            throw new CustomException("非法输入");
        }
        return Result.ok(this.getProductDtoById(id));
    }

    @Override
    @Transactional
    public Result updateWithRedis(Product product) {
        if(null==product){
            throw new CustomException("非法输入");
        }
        //1.更新输入库表Product
        var isUpdate = updateById(product);
        if(!isUpdate){
           return Result.fail("更新失败");
        }
        //2.更新List中该product顺序并删除相关缓存信息
        var productId = product.getId();
        var delKey = CACHE_PRODUCT_PREFIX + productId + CACHE_PRODUCT_INFO_SUFFIX;
        var success = redisCacheUtils.updateListMoreElement(PRODUCT_PAGE_IDS_KEY, delKey, productId, CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
        if(!success){
            throw new SystemException("Redis更新服务异常");
        }
        return Result.ok("更新成功");
    }

    @Override
    public synchronized Result deleteByIdsWithRedis(List<Long> ids) {
        if(0==ids.size()){
            throw new CustomException("无id数据");
        }
        //1.根据id删除数据库中对应product数据
        var isRemove = removeByIds(ids);
        if(!isRemove){
            throw new CustomException("删除失败");
        }
        var delKeysPatternBuilder = new StringBuilder(CACHE_PRODUCT_PREFIX);
        ids.forEach(id->{
            //2.根据id在Redis中查询img字段是否有值
            var image = getProductDtoById(id).getImage();
            //3.删除Redis中List中的id与相关的缓存数据
            var success = redisCacheUtils.deleteListMoreElement(PRODUCT_PAGE_IDS_KEY, delKeysPatternBuilder.append(id).toString(), id, CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
            delKeysPatternBuilder.delete(CACHE_PRODUCT_PREFIX.length(),delKeysPatternBuilder.length());
            if(!success){
                throw new SystemException("Redis中List删除功能异常");
            }
            //4.如果有值则需要删除服务器上对应的图片
            //当数据库和Redis中数据都成功删除时才删除对应的图片文件
            if (StringUtils.isNotEmpty(image)) {
                //有图片
                //file=../img/xxx.xxx
                //根据ids删除服务器中对应的图片
                File file = new File(BATH_PATH + image);
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

    @Override
    public Result listByCategoryId(Long categoryId) {
        if(categoryId==null){
            throw new CustomException("非法输入");
        }
        var productDtoList = getProductIdListWithRedis()        //1.先查询所有的产品List中的id
                .stream().map(this::getProductDtoById)      //2.获取每个productDto对象
                .filter(i -> i.getCategoryId().equals(categoryId))   //过滤categoryId条件
                .collect(Collectors.toList());
        if(productDtoList.isEmpty()){
            return Result.ok(List.of());
        }
        //3.返回
        return Result.ok(productDtoList);
    }

    public ProductDto getProductDtoById(Long id) {
        //1.判断id是否为空
        if (id == null) {
            return null;
        }
        var productDto = new ProductDto();
        //根据id在Redis中查询Product对应数据
        var product = redisCacheUtils.avalancheAndPenetration(
                RedisKey.create(id, CACHE_PRODUCT_PREFIX, CACHE_PRODUCT_INFO_SUFFIX),
                Product.class,
                this::getById,
                CACHE_PRODUCT_TTL,
                TimeUnit.MINUTES);
        if(product==null){
            //空缓存,返回空对象即可
            return productDto;
        }
        BeanUtil.copyProperties(product,productDto);
        //在Redis中查询分类名并设置
        productDto.setCategoryName(categoryService.getCategoryById(product.getCategoryId()).getName());
        return productDto;
    }

    private List<Long> getProductIdListWithRedis(){
        //构造条件
        var queryWrapper = new LambdaQueryWrapper<Product>().select(Product::getId).orderByDesc(Product::getUpdateTime);
        //调用Redis查询productId List
        var key = RedisKey.create(CACHE_ALL_PRODUCT_IDS, CACHE_PAGE_PRODUCTS_PREFIX, BLANK_STRING);
        return  redisCacheUtils.avalancheAndPenetrationWithPage(
                key,
                i -> this.list(queryWrapper).stream().map(Product::getId).collect(Collectors.toList()),
                queryWrapper,
                CACHE_PAGE_DEFAULT_TTL,
                TimeUnit.MINUTES
        ).stream().map(Long::valueOf).collect(Collectors.toList());
    }
}




