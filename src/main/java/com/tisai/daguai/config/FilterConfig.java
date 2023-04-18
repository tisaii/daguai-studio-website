package com.tisai.daguai.config;

import com.tisai.daguai.filter.BackendTokenFreshFilter;
import com.tisai.daguai.filter.LoginCheckFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import javax.servlet.Filter;

@Configuration
public class FilterConfig {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Bean
    public FilterRegistrationBean<Filter> backendTokenFreshFilter(){
        var bean = new FilterRegistrationBean<>();
        bean.setFilter(new BackendTokenFreshFilter(stringRedisTemplate));
        bean.addUrlPatterns("/backend/*");
        bean.setOrder(0);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<Filter> loginCheckFilter(){
        var bean = new FilterRegistrationBean<>();
        bean.setFilter(new LoginCheckFilter());
        bean.addUrlPatterns("/backend/*");
        bean.setOrder(1);
        return bean;
    }
}
