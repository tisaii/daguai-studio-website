package com.tisai.daguai.filter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.tisai.daguai.dto.AdminTokenDto;
import com.tisai.daguai.utils.AdminHolder;
import com.tisai.daguai.utils.RedisContents;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.tisai.daguai.utils.SystemContents.REQUEST_AUTHORIZATION_HEADER;

@Slf4j
//@WebFilter(filterName = "backendTokenFreshFilter",urlPatterns = "/backend/*")
public class BackendTokenFreshFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    public BackendTokenFreshFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req= (HttpServletRequest) request;
        HttpServletResponse resp= (HttpServletResponse) response;

//        log.debug("BackendTokenFreshFilter");
        //获取本次请求的token
        var authorization = req.getHeader(REQUEST_AUTHORIZATION_HEADER);
//        log.debug("token flush debug:");
//        log.debug("authorization:{}",authorization);
        if(StrUtil.isBlank(authorization)){
            AdminHolder.removeAdmin();
            chain.doFilter(req,resp);
            return;
        }
        //接受到的Authorization是Bearer XXXXX,中间空格分隔
        var strings = authorization.split(" ");
        //数组第二个元素即为token
        var token = strings[1];
//        log.debug("token:{}",token);
        //从Redis中根据token取值
        var key = RedisContents.LOGIN_ADMIN_TOKEN_KEY + token;
//        log.debug("key:{}",key);
        var objectMap = stringRedisTemplate.opsForHash().entries(key);
//        log.debug("objectMap:{}",objectMap);
        if(objectMap.isEmpty()){
            //map为空, 即Redis中无此token, 直接返回
            AdminHolder.removeAdmin();
            chain.doFilter(req,resp);
            return;
        }

        //有此token, 先把用户信息存到ThreadLocal中, 再刷新token
        //将map转化为AdminTokenDto
        var adminTokenDto = BeanUtil.fillBeanWithMap(objectMap, new AdminTokenDto(), false);
//        log.debug("adminTokenDto:{}",adminTokenDto);
        //存到ThreadLocal中
        AdminHolder.saveAdmin(adminTokenDto);
//        log.debug("存入到threadLocal中,admin对象为:{}",AdminHolder.getAdmin());
        //刷新token
        stringRedisTemplate.expire(key,30, TimeUnit.MINUTES);
        chain.doFilter(req,resp);
    }

    @Override
    public void destroy() {

    }

}
