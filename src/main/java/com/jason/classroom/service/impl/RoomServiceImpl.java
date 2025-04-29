package com.jason.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.Room;
import com.jason.classroom.entity.Sign;
import com.jason.classroom.entity.Teacher;
import com.jason.classroom.entity.User;
import com.jason.classroom.mapper.RoomMapper;
import com.jason.classroom.service.RoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类，处理教室相关操作
 * </p>
 *
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    @Resource
    private RoomMapper roomMapper;

    /**
     * 获取所有教室列表
     * @return Map 返回包含教室数据的Map
     */
    @Override
    public Map<String,Object> roomList() {
        Map<String,Object> map = new HashMap<>();
        List<Map<String, Object>> rooms = roomMapper.selectMaps(new QueryWrapper<Room>());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Map<String, Object> room : rooms) {
            Object startObj = room.get("starttime");
            Object endObj = room.get("endtime");
            if (startObj instanceof Timestamp) {
                LocalDateTime start = ((Timestamp) startObj).toLocalDateTime();
                room.put("starttime", formatter.format(start));
            }
            if (endObj instanceof Timestamp) {
                LocalDateTime end = ((Timestamp) endObj).toLocalDateTime();
                room.put("endtime", formatter.format(end));
            }
        }
        map.put("data", rooms);
        map.put("code", 0);
        map.put("msg", "获取数据成功");
        return map;
    }


    /**
     * 获取指定学校的所有教室
     *
     * @param request HttpServletRequest对象，获取session中的teacher或user信息
     * @return Result 返回教室列表的成功结果
     */
    @Override
    public Result getRoomsBySchool(HttpServletRequest request) {
        // 获取session中的教师信息
        Teacher teacher = (Teacher) request.getSession().getAttribute("teacher");
        List<Room> rooms = new ArrayList<>();

        // 如果存在教师信息，查询教师所在学校的教室
        if (teacher != null){
            rooms = roomMapper.selectList(new QueryWrapper<Room>().eq("school", teacher.getSchool()));
        } else {
            // 如果不存在教师信息，尝试从用户信息中获取教室
            User user = (User) request.getSession().getAttribute("user");
            rooms = roomMapper.selectList(new QueryWrapper<Room>().eq("school", user.getSchool()));
        }

        // 将所有教室编号添加到列表中
        ArrayList<String> list = new ArrayList<>();
        rooms.forEach(item -> {
            list.add(item.getNum());
        });

        // 返回成功结果
        return Result.succ(list);
    }

    /**
     * 根据教室状态获取教室列表
     *
     * @param status 教室状态，0表示空闲，1表示被占用等
     * @return Result 返回符合状态的教室列表
     */
    @Override
    public Result getRoomsByStatus(Integer status) {
        // 查询所有符合状态的教室
        List<Room> rooms = roomMapper.selectList(new QueryWrapper<Room>().eq("status", status));
        return Result.succ(rooms);
    }

    /**
     * 预约教室（占用）
     *
     * @param room 传入的教室对象，包括开始时间、结束时间等信息
     * @return Result 处理结果，如果可以占用返回成功，否则返回失败信息
     */
    @Override
    public Result Occupy(Room room) {
        // 获取当前自习室的开始时间
        LocalDateTime starttime = room.getStarttime();
        // 根据教室ID查询已有的教室信息
        Room selectById = roomMapper.selectById(room.getId());
        LocalDateTime selectByIdEndtime = selectById.getEndtime();
        // 判断自习室在所选时间段内是否已被占用
        if (starttime.isAfter(selectByIdEndtime)){
            room.setStatus(0); // 设置教室状态为占用状态
            // 更新教室信息
            this.updateById(room);
            // 返回占用成功结果
            return Result.succ(room);
        }
        // 如果无法预约，返回失败信息
        return Result.fail("该自习室在指定时段无法预约");
    }

}
