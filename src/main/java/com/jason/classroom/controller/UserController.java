package com.jason.classroom.controller;



import cn.hutool.core.map.MapUtil;
import com.jason.classroom.common.dto.LoginDTO;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.User;
import com.jason.classroom.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  前端控制器
 * </p>
 */@RestController
@RequestMapping("/user")
@Api(tags = {"用户接口"})
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录接口
     * 该接口用于获取用户的基本信息，通过用户ID查询并返回
     * @return 返回用户信息
     */
    @GetMapping("/index")
    @ApiOperation(value = "用户登录接口")
    public Result index(){
        User user = userService.getById(1);  // 获取ID为1的用户信息（示例）
        return Result.succ(user);
    }

    /**
     * 用户注册接口
     * 该接口用于用户注册，传入用户名、密码和学校信息进行注册
     * @param user 用户对象，包含用户名、密码和学校信息
     * @return 返回注册结果
     */
    @PostMapping("/save")
    @ApiOperation(value = "用户注册接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名/账号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "school", value = "所属学校", required = true)
    })
    public Result save(@Validated @RequestBody User user) {
        return userService.regist(user);
    }

    /**
     * 用户登录接口
     * 该接口用于用户登录验证，通过用户名和密码进行登录，成功后返回用户信息
     * @param loginDTO 登录信息对象，包含用户名和密码
     * @param request 请求对象，保存用户登录状态
     * @param response 响应对象，设置登录相关的cookie等信息
     * @return 返回登录结果，包含用户的基本信息
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名/账号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true)
    })
    public Result login(@Validated @RequestBody LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response){
        User user = userService.login(loginDTO, response);
        if(user == null){
            return Result.fail("账号或密码错误");
        }
        // 将用户信息存入session
        request.getSession().setAttribute("user", user);
        // 返回用户的基本信息
        return Result.succ(MapUtil.builder()
                .put("id", user.getId())
                .put("username", user.getUsername())
                .put("school", user.getSchool())
                .map()
        );
    }

    /**
     * 用户注销登录接口
     * 该接口用于用户注销登录，清除登录状态
     * @return 返回注销结果
     */
    @RequiresAuthentication
    @GetMapping("/logout")
    @ApiOperation(value = "注销")
    public Result logout(){
        SecurityUtils.getSubject().logout();  // 注销当前会话
        return Result.succ(null);
    }

    /**
     * 获取用户信息
     * 该接口返回当前登录用户的详细信息
     * @return 返回用户对象，包含用户的基本信息
     */
    @GetMapping("/userinfo")
    @ApiOperation(value = "获取用户信息接口")
    public User getUserInfo(HttpServletRequest request){
        return userService.userinfo(request);
    }

    /**
     * 修改用户信息
     * 该接口用于修改用户的个人信息
     * @param user 用户对象，包含修改后的信息
     * @return 返回修改结果
     */
    @PostMapping("chuserInfo")
    @ApiOperation(value = "修改用户信息接口")
    public Result chUserInfo(User user){
        return userService.chUserInfo(user);
    }

    /**
     * 获取用户名用于显示登录状态
     * 该接口获取当前登录用户的基本信息，主要用于显示登录状态中的用户名
     * @param request 请求对象，包含当前登录用户的信息
     * @return 返回当前登录用户的基本信息
     */
    @GetMapping("/getusername")
    @ApiOperation(value ="获取用户名用")
    public Object getUserName(HttpServletRequest request){
        Object user = request.getSession().getAttribute("user");
        return user;
    }
}
