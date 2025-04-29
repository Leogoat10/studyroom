package com.jason.classroom.mapper;

import com.jason.classroom.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 */
public interface UserMapper extends BaseMapper<User> {

    User selectByUsername(String username);
}
