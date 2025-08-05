package com.jason.classroom.controller;


import cn.hutool.core.map.MapUtil;
import com.jason.classroom.common.dto.LoginDTO;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.Admin;
import com.jason.classroom.entity.Room;
import com.jason.classroom.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 */
@RestController
@RequestMapping("/admin")
@Api(tags = {"管理员接口"}) // 用于Swagger生成接口文档，标签用于标识接口模块
public class AdminController {

    @Autowired
    private AdminService adminService; // 注入AdminService

    /**
     * 管理员登录接口
     * @param loginDTO 登录信息传输对象
     * @param response HTTP响应
     * @return 登录结果
     */
    @PostMapping("/login")
    @ApiOperation(value = "管理员登录接口") // 接口的描述信息
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "管理员账号", required = true), // 用户名参数
            @ApiImplicitParam(name = "password", value = "密码", required = true) // 密码参数
    })
    public Result login(@Validated @RequestBody LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        // 调用AdminService的login方法，进行管理员登录操作
        Admin admin = adminService.login(loginDTO, response);
        if (admin == null) {
            // 登录失败，返回错误信息
            return Result.fail("账号或密码错误");
        }
        // 登录成功，将管理员信息存入session
        request.getSession().setAttribute("admin", admin);
        return Result.succ(MapUtil.builder()
                .put("id", admin.getId())
                .put("username", admin.getUsername())
                .map()
        );
    }

    /**
     * 管理员注销登录接口
     * @return 注销结果
     */
    @GetMapping("/logout")
    @ApiOperation(value = "管理员注销接口")
    public Result logout() {
        // 调用Shiro的logout方法进行注销
        SecurityUtils.getSubject().logout();
        return Result.succ(null); // 返回成功信息
    }

    /**
     * 获取管理员信息接口
     * @param username 管理员用户名
     * @return 管理员信息
     */
    @GetMapping("/adminInfo")
    @ApiOperation(value = "获取管理员信息接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "管理员账号", required = true) // 用户名参数
    })
    public Result adminInfo(String username) {
        // 调用adminService获取管理员信息
        return adminService.adminInfo(username);
    }

    /**
     * 获取当前管理员的用户名
     * @param request HTTP请求，获取session中的admin对象
     * @return 当前管理员信息
     */
    @GetMapping("/getadminname")
    @ApiOperation(value = "获取管理员用户名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "session", required = true) // 请求参数
    })
    public Object getAdminName(HttpServletRequest request) {
        // 从session中获取admin对象
        Object admin = request.getSession().getAttribute("admin");
        return admin;
    }

    /**
     * 获取全部用户列表
     * @return 用户列表
     */
    @GetMapping("/userlist")
    @ApiOperation(value = "获取用户信息列表")
    public Map<String, Object> userList() {
        // 调用adminService获取所有用户信息
        return adminService.userList();
    }

    /**
     * 获取全部教室列表
     * @return 教室列表
     */
    @GetMapping("/roomlist")
    @ApiOperation(value = "获取教室列表")
    public Map<String, Object> roomList() {
        // 调用adminService获取所有教室信息
        return adminService.roomList();
    }

    /**
     * 删除自习室
     * @param roomnum 自习室编号
     */
    @PostMapping("/deleteRoom")
    @ApiOperation(value = "删除自习室")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "num", value = "自习室编号", required = true) // 教室编号参数
    })
    public void deleteRoom(@RequestParam("num") String roomnum) {
        // 调用adminService删除指定编号的自习室
        adminService.deleteRoom(roomnum);
    }

    /**
     * 添加自习室
     * @param room 自习室信息
     * @return 添加结果
     */
    @PostMapping("/addRoom")
    @ApiOperation(value = "添加自习室")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "room", value = "自习室信息", required = true) // 自习室信息参数
    })
    public Result addRoom(Room room) {
        // 调用adminService添加自习室
        return adminService.addRoom(room);
    }

    /**
     * 编辑自习室信息
     * @param capacity 容量
     * @param school 学校
     * @param num 教室号
     * @param status 状态
     */
    @PostMapping("/editRoom")
    @ApiOperation(value = "编辑自习室信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "originalNum", value = "原始教室号", required = true),
            @ApiImplicitParam(name = "num", value = "新教室号", required = true),
            @ApiImplicitParam(name = "capacity", value = "容量", required = true),
            @ApiImplicitParam(name = "school", value = "学校", required = true),
            @ApiImplicitParam(name = "status", value = "状态", required = true)
    })
    public Result editRoom(@RequestParam("originalNum") String originalNum,
                           @RequestParam("num") String num,
                           @RequestParam("capacity") Integer capacity,
                           @RequestParam("school") String school,
                           @RequestParam("status") Integer status) {
        // 调用adminService编辑指定教室信息
        return adminService.editRoom(originalNum, num, capacity, school, status);
    }

    @PostMapping("/updatePoint")
    @ApiOperation(value = "更新用户积分")
    public Result updatePoints(@RequestParam("Username") String Username,
                               @RequestParam("points") int points) {
        return adminService.updatePoints(Username,points);
    }
}
