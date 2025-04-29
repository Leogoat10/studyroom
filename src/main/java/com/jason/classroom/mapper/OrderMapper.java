package com.jason.classroom.mapper;

import com.jason.classroom.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>

 */
public interface OrderMapper extends BaseMapper<Order> {
    /*
    取消预约根据num编号取消
    * */
    Order selectByNum(String num);
}
