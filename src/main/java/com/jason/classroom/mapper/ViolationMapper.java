package com.jason.classroom.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jason.classroom.entity.Violation;

/**
 * <p>
 *  Mapper 接口
 * </p>

 */
public interface ViolationMapper extends BaseMapper<Violation> {
    Violation selectByUsername(String username);
}