package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebFault;
import java.io.IOException;

/**
 * 登录过滤器，拦截所有请求，判断是否登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();//路径匹配器，支持通配符写法"/**"

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1.获取本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}", requestURI);//控制台输出日志方便调试
        String[] urls = new String[]{   //放入不需要处理的路径
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };

        //2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //3.如果不用处理直接放行
        if(check){
            log.info("本次{}请求不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //4.判断登录状态，如果已经登录，直接放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录，用户id为{}", request.getSession().getAttribute("employee"));
            filterChain.doFilter(request, response);
            return;
        }

        //5.如果未登录返回未登录结果,通过输出流的方式向客户端页面响应数据，让前端判断页面跳转
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }



    /**
     * 路径匹配方法，判断请求路径是否在不需要处理的路径中
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI){
        for (String url : urls){
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }

}
