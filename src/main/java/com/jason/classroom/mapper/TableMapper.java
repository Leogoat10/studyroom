package com.jason.classroom.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jason.classroom.entity.Order;
import com.jason.classroom.entity.Table;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>

 */
public interface TableMapper extends BaseMapper<Table> {
    int insertBatch(@Param("list") List<Table> tables);


}
