package com.jason.classroom.service;

import com.jason.classroom.common.dto.LoginDTO;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.Admin;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jason.classroom.entity.Room;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>

 */
public interface AdminService extends IService<Admin> {

    public Admin login(LoginDTO loginDTO, HttpServletResponse response);

    public Map<String,Object> userList();

    public Map<String,Object> roomList();

    public void deleteRoom(String roomnum);

    public Result addRoom(Room room);

    public Result editRoom(String originalNum, String num, Integer capacity, String school, Integer status);

    public Result adminInfo(String username);

    public Result updatePoints(String Username, int points);

}
