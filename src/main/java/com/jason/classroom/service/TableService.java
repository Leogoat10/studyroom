package com.jason.classroom.service;

import com.jason.classroom.common.lang.Result;
import com.jason.classroom.common.vo.TableVO;
import com.jason.classroom.entity.Table;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>

 */
public interface TableService extends IService<Table> {
    public Result table4List(String room);

    public Result orderTable(Table table);

    public Result getAbleTable(String tablenum);

    public Result updateTable(TableVO tableVO);

}
