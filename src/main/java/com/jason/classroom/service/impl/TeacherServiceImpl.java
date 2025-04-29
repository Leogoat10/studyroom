package com.jason.classroom.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jason.classroom.common.dto.LoginDTO;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.Admin;
import com.jason.classroom.entity.Teacher;
import com.jason.classroom.entity.User;
import com.jason.classroom.mapper.TeacherMapper;
import com.jason.classroom.service.TeacherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jason.classroom.util.JwtUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 */@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    @Resource
    private TeacherMapper teacherMapper; // 教师数据访问接口
    @Resource
    private JwtUtils jwtUtils; // JWT工具类，用于生成和验证JWT

    /**
     * 教师登录
     *
     * @param loginDTO 登录请求数据
     * @param response HTTP响应对象
     * @return Teacher 登录成功返回教师信息，失败返回null
     */
    @Override
    public Teacher login(LoginDTO loginDTO, HttpServletResponse response) {
        // 根据用户名查找教师
        Teacher teacher = this.getOne(new QueryWrapper<Teacher>().eq("username", loginDTO.getUsername()));

        // 如果教师不存在或者密码不正确，返回null
        if (teacher == null || !teacher.getPassword().equals(loginDTO.getPassword())) {
            return null;
        }

        // 使用JWT工具类生成token
        String jwt = jwtUtils.generateToken(teacher.getId());

        // 设置响应头，包含Authorization字段携带JWT
        response.setHeader("Authorization", jwt);
        response.setHeader("Access-control-Expose-Headers", "Authorization");

        return teacher; // 登录成功，返回教师信息
    }

    /**
     * 修改教师信息
     *
     * @param teacher 教师修改后的信息
     * @return Result 返回操作结果
     */
    @Override
    public Result chInfo(Teacher teacher) {
        // 根据用户名更新教师信息
        this.update(teacher, new QueryWrapper<Teacher>().eq("username", teacher.getUsername()));

        // 返回修改成功的结果
        return Result.succ("信息修改成功");
    }

    /**
     * 教师注册
     *
     * @param teacher 教师注册信息
     * @return Result 返回操作结果
     */
    @Override
    public Result regist(Teacher teacher) {
        // 查询数据库中是否已存在该用户名
        if (teacherMapper.selectCount(new QueryWrapper<Teacher>().eq("username", teacher.getUsername())) == 1) {
            return Result.fail("该教师用户已存在"); // 如果已存在，则返回失败信息
        }

        // 如果教师用户不存在，则进行注册操作
        this.save(teacher); // 保存教师信息

        // 注册成功，返回教师信息
        return Result.succ(200, "注册成功", MapUtil.builder()
                .put("id", teacher.getId())
                .put("username", teacher.getUsername())
                .put("phone", teacher.getPhone())
                .put("school", teacher.getSchool())
                .map());
    }

    /**
     * 获取教师信息
     *
     * @param request HTTP请求对象
     * @return Teacher 返回教师的详细信息
     */
    @Override
    public Teacher teacherInfo(HttpServletRequest request) {
        // 从Session中获取教师信息
        Teacher teacher = (Teacher) request.getSession().getAttribute("teacher");

        // 如果教师用户名为空，返回null
        if (StringUtils.isEmpty(teacher.getUsername())) {
            return null;
        } else {
            // 查询数据库获取教师的详细信息
            Teacher newteacher = teacherMapper.selectOne(new QueryWrapper<Teacher>().eq("username", teacher.getUsername()));
            return newteacher; // 返回教师信息
        }
    }
}

