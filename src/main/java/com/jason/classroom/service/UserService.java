package com.jason.classroom.service;

import com.jason.classroom.common.dto.LoginDTO;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface UserService extends IService<User> {

    public User login(LoginDTO loginDTO, HttpServletResponse response);

    public Result regist(User user);

    public User userinfo(HttpServletRequest request);

    public Result chUserInfo(User user);
}
