package com.jason.classroom.service;

import cn.hutool.http.HttpRequest;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.Room;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>

 */
public interface RoomService extends IService<Room> {

    public Map<String,Object> roomList();


    public Result getRoomsBySchool(HttpServletRequest request);

    public Result getRoomsByStatus(Integer status);

    public Result Occupy(Room room);
}
