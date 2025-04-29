package com.jason.classroom.shiro;

import cn.hutool.json.JSONUtil;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends AuthenticatingFilter {

    @Autowired
    JwtUtils jwtUtils;  // JWT工具类，用于处理Token的解析和验证

    /**
     * 创建Token
     * 从请求头中获取Authorization字段中的JWT，若有Token则返回JwtToken对象，若没有则返回null
     * @param servletRequest 请求对象
     * @param servletResponse 响应对象
     * @return 返回JwtToken对象，或者返回null表示无Token
     * @throws Exception 异常
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = request.getHeader("Authorization");

        // 如果token为空，则返回null
        if (StringUtils.isEmpty(jwt)){
            return null;
        }

        // 创建JwtToken对象并返回
        return new JwtToken(jwt);
    }

    /**
     * 校验凭证和处理过期的Token
     * 该方法主要用来验证请求中的JWT Token是否合法，如果合法则执行登录，若过期则抛出异常
     * @param servletRequest 请求对象
     * @param servletResponse 响应对象
     * @return 如果Token合法，则继续执行登录，否则返回登录失败
     * @throws Exception 异常
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = request.getHeader("Authorization");

        // 如果Token为空，则直接返回true，表示放行
        if (StringUtils.isEmpty(jwt)){
            return true;
        } else {
            // 校验JWT是否有效
            Claims claim = jwtUtils.getClaimByToken(jwt);
            // 如果Token无效或已过期，抛出过期凭证异常
            if (claim == null || jwtUtils.isTokenExpired(claim.getExpiration())){
                throw new ExpiredCredentialsException("token已失效，请重新登录");
            }

            // 如果Token有效，执行登录
            return executeLogin(servletRequest, servletResponse);
        }
    }

    /**
     * 登录失败时的处理
     * 当执行登录失败时，返回JSON格式的错误信息给客户端
     * @param token 认证Token
     * @param e 认证异常
     * @param request 请求对象
     * @param response 响应对象
     * @return 登录失败时返回false，表示认证失败
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // 获取异常的根本原因
        Throwable throwable = e.getCause() == null ? e : e.getCause();

        // 创建一个失败的Result对象并转换为JSON
        Result result = Result.fail(throwable.getMessage());
        String json = JSONUtil.toJsonStr(result);

        try {
            // 将失败信息写入响应流
            httpServletResponse.getWriter().print(json);
        } catch (IOException ioException) {
            // 如果写入失败，可以在此记录日志或其他处理
        }
        return false;  // 返回false表示登录失败
    }

    /**
     * 解决跨域问题
     * 该方法用于处理跨域请求，设置必要的HTTP头信息来支持跨域
     * @param request 请求对象
     * @param response 响应对象
     * @return 是否继续处理请求
     * @throws Exception 异常
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {

        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);

        // 设置跨域请求的相关Header
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));

        // 如果是OPTIONS预检请求，直接返回正常响应
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(org.springframework.http.HttpStatus.OK.value());
            return false;
        }

        // 调用父类的preHandle方法
        return super.preHandle(request, response);
    }
}

