package com.tisai.daguai;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tisai.daguai.domain.Blog;
import com.tisai.daguai.domain.BlogProduct;
import com.tisai.daguai.domain.Feedback;
import com.tisai.daguai.dto.RedisKey;
import com.tisai.daguai.dto.ZSETDto;
import com.tisai.daguai.mapper.FeedbackMapper;
import com.tisai.daguai.service.BlogProductService;
import com.tisai.daguai.service.BlogService;
import com.tisai.daguai.service.FeedbackService;
import com.tisai.daguai.utils.RedisCacheUtils;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.tisai.daguai.utils.RedisContents.*;
import static com.tisai.daguai.utils.RedisContents.CACHE_BLOG_WITH_PRODUCT_TTL;

@SpringBootTest
public class DaguaiTest {

    @Resource
    private BlogService blogService;

    @Resource
    private RedisCacheUtils redisCacheUtils;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private BlogProductService blogProductService;

    @Resource
    private FeedbackService feedbackService;

    @Resource
    private FeedbackMapper feedbackMapper;


    @Test
    public void splitTest(){
        String test="{\"id\":\"chatcmpl-6qMYlej1F4yxeMEtBgBKfK3WlqADl\",\"object\":\"chat.completion\",\"created\":1677937795,\"model\":\"gpt-3.5-turbo-0301\",\"usage\":{\"prompt_tokens\":9,\"completion_tokens\":19,\"total_tokens\":28},\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"你好！有什么需要我帮助的吗？\"},\"finish_reason\":\"stop\",\"index\":0}]}\n";
//        System.out.println(test);
        int endIndex = test.lastIndexOf("finish_reason")-4;
        int beginIndex = test.lastIndexOf(":", endIndex)+2;
        String substring = test.substring(beginIndex,endIndex);
        System.out.println(substring);
    }

    @Test
    public void RedisCacheSelectRedictTest(){
        Long id = 1630569618293985281L;
        //2.根据id查询基本博客信息(采用Redis)
/*        var blog = redisCacheUtils.avalancheAndPenetrationIn(
                id,
                CACHE_BLOG_PREFIX,
                CACHE_BLOG_BASIC_INFO_SUFFIX,
                Blog.class, blogService::getById,
                CACHE_BLOG_BASIC_INFO_TTL,
                TimeUnit.MINUTES);


        //3.根据博客id查询BlogProduct表中的所有关联信息


        var list = redisCacheUtils.avalancheAndPenetrationIn(
                id, CACHE_BLOG_PREFIX, CACHE_BLOG_WITH_PRODUCT_SUFFIX, new ArrayList<BlogProduct>(){},
                i -> {
//                    return blogProductService.list(new LambdaQueryWrapper<BlogProduct>().eq(BlogProduct::getBlogId,i));
                    var queryWrapper = new LambdaQueryWrapper<BlogProduct>();
                    queryWrapper.eq(BlogProduct::getBlogId, i);
                    return blogProductService.list(queryWrapper);
                }, CACHE_BLOG_WITH_PRODUCT_TTL, TimeUnit.MINUTES
        );*/
//        var queryWrapper = new LambdaQueryWrapper<Feedback>().select(Feedback::getId).select(Feedback::getCreateTime);

//        redisCacheUtils.avalancheAndPenetrationWithPage(RedisKey.create("z1","",""),)
//        var zsetDtos = feedbackMapper.selectIdAndCreateTimeZsetDtos();
        var feedbackIds = redisCacheUtils.avalancheAndPenetrationWithPageZSET(
                RedisKey.create("ids:", "cache:zset:page:feedbacks:", ""),
                feedbackMapper::selectIdAndCreateTimeZsetDtos,
                LocalDateTime.of(2022,3,2,0,0,0).toEpochSecond(ZoneOffset.UTC),
                LocalDateTime.of(2023,3,2,0,0,0).toEpochSecond(ZoneOffset.UTC),
                1,
                5,
                CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
        var ods = feedbackIds.getDataList();
    }

    @Test
    public void luaTest(){
        var script = new DefaultRedisScript<Long>();
        script=new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setLocation(new ClassPathResource("lua/test.lua"));

        var execute = stringRedisTemplate.execute(script, List.of(), "");
    }
}
