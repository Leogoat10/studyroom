package com.jason.classroom.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jason.classroom.common.dto.LoginDTO;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.Teacher;
import com.jason.classroom.entity.User;
import com.jason.classroom.service.TeacherService;
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
@RequestMapping("/teacher")
@Api(tags = {"教师接口"})
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private UserController userController;

    /**
     * 教师登录接口
     * 该接口用于教师登录验证，通过用户名和密码进行登录，成功后返回教师信息
     * @param loginDTO 登录信息对象，包含用户名和密码
     * @param request 请求对象，保存教师登录状态
     * @param response 响应对象，设置登录相关的cookie等信息
     * @return 返回登录结果，包含教师的基本信息
     */
    @PostMapping("/login")
    @ApiOperation(value = "教师登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名/账号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true)
    })
    public Result login(@Validated @RequestBody LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response){
        Teacher teacher = teacherService.login(loginDTO, response);
        if(teacher == null){
            return Result.fail("账号或密码错误");
        }
        // 将教师信息存入session
        request.getSession().setAttribute("teacher", teacher);
        // 返回教师的基本信息
        return Result.succ(MapUtil.builder()
                .put("id", teacher.getId())
                .put("username", teacher.getUsername())
                .put("school", teacher.getSchool())
                .map()
        );
    }

    /**
     * 教师注册接口
     * 该接口用于教师注册，通过提供用户名、密码和学校信息进行教师的注册
     * @param teacher 教师对象，包含用户名、密码和所属学校
     * @return 返回注册结果
     */
    @PostMapping("/save")
    @ApiOperation(value = "教师注册接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "教师用户名/账号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "school", value = "所属学校", required = true)
    })
    public Result save(@Validated @RequestBody Teacher teacher) {
        return teacherService.regist(teacher);
    }

    /**
     * 教师注销登录接口
     * 该接口用于教师注销登录，清除登录状态
     * @return 返回注销结果
     */
    @RequiresAuthentication
    @GetMapping("/logout")
    public Result logout(){
        SecurityUtils.getSubject().logout();  // 注销当前会话
        return Result.succ(null);
    }

    /**
     * 获取教师信息
     * 该接口返回当前登录教师的详细信息
     * @return 返回教师对象，包含教师的基本信息
     */
    @GetMapping("/teacherInfo")
    public Teacher getTeacherInfo(HttpServletRequest request){
        Teacher teacher = teacherService.teacherInfo(request);
        return teacher;
    }

    /**
     * 修改教师信息
     * 该接口用于修改教师的个人信息
     * @param teacher 教师对象，包含修改后的信息
     * @return 返回修改结果
     */
    @PostMapping("/chTeacherInfo")
    public Result chInfo(Teacher teacher){
        return teacherService.chInfo(teacher);
    }

    /**
     * 获取教师用户名用于显示登录状态
     * 该接口获取当前登录教师的基本信息，主要用于显示登录状态中的教师姓名
     * @param request 请求对象，包含当前登录教师的信息
     * @return 返回当前登录教师的基本信息
     */
    @GetMapping("/getteachername")
    @ApiOperation(value = "获取教师姓名")
    public Object getUserName(HttpServletRequest request){
        Object teacher = request.getSession().getAttribute("teacher");
        return teacher;
    }

}
