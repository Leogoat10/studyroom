package com.jason.classroom.controller;


import com.jason.classroom.common.lang.Result;
import com.jason.classroom.service.SignService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  前端控制器
 * </p>
 */@RestController
@RequestMapping("/sign")
@Api(tags = {"签到接口"})
public class SignController {

    @Autowired
    private SignService signService;

    /**
     * 用户签到
     * 该接口用于处理用户签到请求
     * @param request 请求对象，包含用户信息
     * @return 返回签到结果
     */
    @GetMapping("/signin")
    @ApiOperation(value = "用户签到接口")
    public Result signIn(HttpServletRequest request){
        return signService.signIn(request);
    }
    /**
     * 用户签退
     * 该接口用于处理用户签退请求
     * @param request 请求对象，包含用户信息
     * @return 返回签退结果
     */
    @GetMapping("/signout")
    @ApiOperation(value = "用户签退接口")
    public Result signOut(HttpServletRequest request){
        return signService.signOut(request);
    }

    /**
     * 获取签到信息
     * 该接口用于获取用户的签到统计信息
     * @param request 请求对象，包含用户信息
     * @return 返回用户签到统计信息
     */
    @GetMapping("/signInfo")
    @ApiOperation(value = "获取用户签到信息接口")
    public Result signInfo(HttpServletRequest request){
        return signService.signCount(request);
    }

    /**
     * 更新签到任务
     * 该接口用于更新签到任务的状态或其他相关信息
     */
    public void updateSignTask(){
        signService.updateSignTask();
    }
}
