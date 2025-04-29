package com.jason.classroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.common.vo.OrderVO;
import com.jason.classroom.common.vo.ViolationVO;
import com.jason.classroom.entity.Violation;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface ViolationService extends IService<Violation> {
    //教师查看违规记录
    public Map<String,Object> violationList();
    //学生查看违规记录
    public Map<String,Object> getViolationByUsername(HttpServletRequest request);
    //学生反馈
    public Result feedback(ViolationVO violationVO, HttpServletRequest request);
    //老师处理
    public Result dispose(ViolationVO violationVO, HttpServletRequest request);
}
