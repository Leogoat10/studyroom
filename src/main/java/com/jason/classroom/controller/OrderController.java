package com.jason.classroom.controller;


import com.jason.classroom.common.lang.Result;
import com.jason.classroom.common.vo.OrderVO;
import com.jason.classroom.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 */@RestController
@RequestMapping("/order")
@Api(tags = {"预约接口"}) // 用于Swagger生成接口文档，标签用于标识预约相关接口模块
public class OrderController {

    @Autowired
    private OrderService orderService; // 注入OrderService，处理与预约相关的业务逻辑

    /**
     * 根据用户名获取预约记录接口
     * @param request HTTP请求
     * @return 返回指定用户的预约记录列表
     */
    @GetMapping("/getOrders")
    @ApiOperation(value = "根据用户名获取预约记录接口")
    public Map<String, Object> getOrdersByUsername(HttpServletRequest request) {
        // 调用orderService获取当前用户的预约记录
        return orderService.getOrdersByUsername(request);
    }

    /**
     * 创建预约接口
     * @param orderVO 预约信息对象
     * @param request HTTP请求
     * @return 返回预约的结果
     */
    @PostMapping("/makeorder")
    @ApiOperation(value = "创建预约接口")
    public Result makeOrder(OrderVO orderVO,  HttpServletRequest request) {
        // 调用orderService处理创建预约的逻辑
        return orderService.makeOrder(orderVO,request);
    }
    /**
    * 取消预约接口
    * */
    @PostMapping("/Cancel")
    @ApiOperation(value = "取消预约接口")
    public Result CancelOrder(OrderVO orderVO, HttpServletRequest request) {
        return orderService.cancelOrder(orderVO,request);
    }
    /**
     * 获取所有预约记录接口
     * @return 返回所有预约记录的列表
     */
    @GetMapping("/orderList")
    @ApiOperation(value = "获取所有预约记录")
    public Map<String, Object> orderList() {
        // 调用orderService获取所有预约记录
        return orderService.orderList();
    }

    /*
     * 根据教室和座位获取当天预约记录
     */
    @GetMapping("/getTodayOrdersBySeat")
    @ApiOperation(value = "获取教室和座位的当天预约记录")
    public Map<String, Object> getTodayOrdersBySeat(
            @RequestParam String room,
            @RequestParam int x,
            @RequestParam int y) {
        return orderService.getTodayOrdersBySeat(room, x, y);
    }
}
