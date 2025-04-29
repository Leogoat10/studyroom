package com.jason.classroom.common.exception;

import com.jason.classroom.common.lang.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j // Lombok 注解，自动生成日志记录器对象 'log'
@RestControllerAdvice // 标注为全局异常处理器，针对所有控制器进行异常处理
public class GlobalExceptionHandler {

    /**
     * 处理非法参数异常（IllegalArgumentException）
     *
     * @param e 异常对象
     * @return 返回一个统一格式的失败响应
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设置响应状态为 400（请求错误）
    @ExceptionHandler(value = IllegalArgumentException.class) // 捕获 IllegalArgumentException 异常
    public Result handler(IllegalArgumentException e) {
        log.error("Assert异常:----------{}", e); // 记录日志，打印异常信息
        return Result.fail(e.getMessage()); // 返回错误信息
    }

    /**
     * 处理 Shiro 权限相关的异常（ShiroException）
     *
     * @param e 异常对象
     * @return 返回一个统一格式的失败响应，状态码为 401
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 设置响应状态为 401（未经授权）
    @ExceptionHandler(value = ShiroException.class) // 捕获 ShiroException 异常
    public Result handler(ShiroException e) {
        log.error("运行时异常:----------{}", e); // 记录日志，打印异常信息
        return Result.fail(401, e.getMessage(), null); // 返回错误信息
    }

    /**
     * 处理通用的运行时异常（RuntimeException）
     *
     * @param e 异常对象
     * @return 返回一个统一格式的失败响应
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设置响应状态为 400（请求错误）
    @ExceptionHandler(value = RuntimeException.class) // 捕获 RuntimeException 异常
    public Result handler(RuntimeException e) {
        log.error("运行时异常:----------{}", e); // 记录日志，打印异常信息
        return Result.fail(e.getMessage()); // 返回错误信息
    }

}

