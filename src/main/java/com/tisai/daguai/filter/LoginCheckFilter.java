package com.tisai.daguai.filter;

import com.alibaba.fastjson.JSON;
import com.tisai.daguai.dto.Result;
import com.tisai.daguai.utils.AdminHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 检查用户是否登录的过滤器
 * 拦截器名称:loginCheckFilter
 * 拦截路径:全部
 */
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器工具类,支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    //设置拦截白名单
        String[] urls = new String[]{
                "/account/login",
                "/account/logout",
                "/admin/**",
                "/user/**",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request= (HttpServletRequest) servletRequest;
        HttpServletResponse response= (HttpServletResponse) servletResponse;

        //1.获取本次请求的URI
        String requestURI = request.getRequestURI().substring(8);
//        log.debug("请求URI:{} ",requestURI);
        //2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //3.如不需要处理,直接放行
        if(check){
            request.getRequestDispatcher(requestURI).forward(request,response);
            return;
        }
        //4-1.判断后台人员登录状态,如果已登录则直接放行, 根据AdminHolder取值判断
        var admin = AdminHolder.getAdmin();
        if(admin!=null){
            //已登录,放行
//            log.debug("admin={}",admin);
            request.getRequestDispatcher(requestURI).forward(request,response);
            return;
        }
        //5.如果未登录则响应未登录结果,通过输出流方式向客户端页面响应数据
        // 前端也有一个响应拦截器,当后端返回code=0且msg=NOTLOGIN时前端就会自动跳转
//        log.debug("用户未登录debug:");
        response.getWriter().write(JSON.toJSONString(Result.fail("NOTLOGIN")));
    }

    /**
     * 路径匹配,检查本次请求是否需要放行
     * @param requestURI request uri
     * @param urls white urls
     * @return t/f
     */
    public boolean check(String[] urls,String requestURI){
        for (String url:urls){
            //match方法判断path是否满足pattern
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }

}
