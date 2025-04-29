package com.jason.classroom.service;

import com.jason.classroom.common.dto.LoginDTO;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.Teacher;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jason.classroom.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>

 */
public interface TeacherService extends IService<Teacher> {

    public Teacher login(LoginDTO loginDTO, HttpServletResponse response);

    public Result chInfo(Teacher teacher);

    public Result regist(Teacher teacher);

    public Teacher teacherInfo(HttpServletRequest request);
}
