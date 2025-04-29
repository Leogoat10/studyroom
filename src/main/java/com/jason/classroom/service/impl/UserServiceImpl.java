package com.jason.classroom.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jason.classroom.common.dto.LoginDTO;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.User;
import com.jason.classroom.mapper.UserMapper;
import com.jason.classroom.service.UserService;
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
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper; // 用户数据访问对象（DAO）
    @Resource
    private JwtUtils jwtUtils; // 用于生成JWT令牌的工具类

    /**
     * 用户登录方法
     *
     * @param loginDTO 登录数据传输对象
     * @param response HTTP响应对象，用于设置响应头
     * @return User 登录成功返回用户对象，失败返回null
     */
    @Override
    public User login(LoginDTO loginDTO, HttpServletResponse response) {
        // 根据用户名查询用户
        User user = this.getOne(new QueryWrapper<User>().eq("username", loginDTO.getUsername()));

        // 如果用户不存在或密码不正确，返回null
        if (user == null || !user.getPassword().equals(loginDTO.getPassword())) {
            return null;
        }

        // 生成JWT令牌
        String jwt = jwtUtils.generateToken(user.getId());

        // 将JWT令牌设置到响应头，以供后续请求验证
        response.setHeader("Authorization", jwt);
        response.setHeader("Access-control-Expose-Headers", "Authorization");

        return user; // 登录成功返回用户对象
    }

    /**
     * 用户注册方法
     *
     * @param user 用户注册信息
     * @return Result 注册操作的结果
     */
    @Override
    public Result regist(User user) {
        // 查询数据库中是否已存在该用户名的用户
        if (userMapper.selectCount(new QueryWrapper<User>().eq("username", user.getUsername())) == 1) {
            return Result.fail("该用户已存在"); // 如果已存在，返回失败
        }

        // 如果不存在，则进行用户创建
        user.setStatus(1); // 设置用户状态为1（激活）
        user.setPoints(100);//默认积分100
        user.setPassword(user.getPassword());

        // 保存新用户到数据库
        this.save(user);

        // 返回成功响应，并包含用户的相关信息
        return Result.succ(200, "注册成功", MapUtil.builder()
                .put("id", user.getId())
                .put("username", user.getUsername())
                .put("phone", user.getPhone())
                .put("school", user.getSchool())
                .map());
    }

    /**
     * 获取当前登录用户的信息
     *
     * @param request HTTP请求对象，包含当前用户的session
     * @return User 返回当前用户的详细信息
     */
    @Override
    public User userinfo(HttpServletRequest request) {
        // 从session中获取当前登录的用户
        User user = (User) request.getSession().getAttribute("user");

        // 如果用户名为空，表示没有登录，返回null
        if (StringUtils.isEmpty(user.getUsername())) {
            return null;
        } else {
            // 根据用户名从数据库查询用户信息并返回
            return userMapper.selectOne(new QueryWrapper<User>().eq("username", user.getUsername()));
        }
    }

    /**
     * 修改用户信息
     *
     * @param user 用户修改后的信息
     * @return Result 修改操作的结果
     */
    @Override
    public Result chUserInfo(User user) {
        if (user == null) {
            log.error("传入修改内容为空");
        }

        // 从数据库查询当前用户的信息
        User selectOne = userMapper.selectOne(new QueryWrapper<User>().eq("username", user.getUsername()));
        if (!selectOne.getPassword().equals(user.getPassword())) {
            user.setPassword(user.getPassword());
        }

        // 更新用户信息
        userMapper.update(user, new QueryWrapper<User>().eq("username", user.getUsername()));

        // 返回成功响应，并包含修改后的用户信息
        return Result.succ(200, "用户信息修改成功", MapUtil.builder()
                .put("id", user.getId())
                .put("username", user.getUsername())
                .put("points",user.getPoints())
                .put("password", user.getPassword())
                .put("email", user.getEmail())
                .put("phone", user.getPhone())
                .put("school", user.getSchool())
                .map());
    }
}

