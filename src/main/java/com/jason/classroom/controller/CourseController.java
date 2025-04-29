package com.jason.classroom.controller;


import com.jason.classroom.common.lang.Result;
import com.jason.classroom.common.vo.CourseVO;
import com.jason.classroom.service.CourseService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>测试
 *  前端控制器
 * </p>
 */
@RestController
@RequestMapping("/course")
@Api(tags = {"课程接口"}) // 用于Swagger生成接口文档，标签用于标识课程相关接口模块
public class CourseController {

    @Autowired
    private CourseService courseService; // 注入CourseService，处理与课程相关的业务逻辑

    /**
     * 获取课程列表接口
     * @param request HTTP请求
     * @return 返回课程列表的Map对象
     */
    @GetMapping("/courselist")
    public Map<String, Object> getCourseList(HttpServletRequest request) {
        // 调用courseService获取课程列表
        return courseService.getCourseList(request);
    }
    /*
    * 根据教室编号获取当天课程信息
    * */
    @GetMapping("/courselistByroomtoday")
    public Map<String, Object> getCourseListByRoomToday(@RequestParam String num) {
        return courseService.getCourseListByRoomToday(num);
    }

    /**
     * 开始课程接口
     * @param courseVO 课程信息对象
     * @param request HTTP请求
     * @return 返回课程启动的结果
     */
    @PostMapping("/startCourse")
    public Result startCourse(CourseVO courseVO, HttpServletRequest request) {
        // 调用courseService处理启动课程的逻辑
        return courseService.startCourse(courseVO, request);
    }

}

