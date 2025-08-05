package com.jason.classroom.controller;

import com.jason.classroom.common.lang.Result;
import com.jason.classroom.common.vo.ViolationVO;
import com.jason.classroom.service.ViolationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/violation")
@Api(tags = {"违规接口"})
public class ViolationController {
    @Autowired
    private ViolationService violationService;
    /*
     * 老师获取违规列表接口
     * */
    @GetMapping("/violationlist")
    @ApiOperation(value = "获取违规列表接口")
    public Map<String ,Object> violationList() {
        return violationService.violationList();
    }
    /*
    * 学生获取违规信息接口
    * */
    @GetMapping("/getByname")
    @ApiOperation(value = "获取违规信息接口")
    public Map<String ,Object> getViolationByUsername(HttpServletRequest request) {
        return violationService.getViolationByUsername(request);
    }
    /*
    学生反馈
    * */
    @PostMapping("/feedback")
    @ApiOperation(value = "学生反馈接口")
    public Result feedback(ViolationVO violationVO, HttpServletRequest request) {
        return violationService.feedback(violationVO, request);
    }
    /*
    * 管理员处理
    * */
    @PostMapping("/dispose")
    @ApiOperation(value = "处理违规接口")
    public Result dispose(ViolationVO violationVO, HttpServletRequest request) {
        return violationService.dispose(violationVO, request);
    }

}
