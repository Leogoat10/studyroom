package com.jason.classroom.controller;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.common.vo.RoomVO;
import com.jason.classroom.entity.Room;
import com.jason.classroom.service.RoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>

 */@RestController
@RequestMapping("/room")
@Api(tags = {"教室接口"})
public class RoomController {
    @Autowired
    private RoomService roomService;

    /**
     * 获取全部教室列表
     * @return 教室列表
     */
    @GetMapping("/roomlist")
    @ApiOperation(value = "获取教室列表")
    public Map<String, Object> roomList() {
        return roomService.roomList();
    }

    /**
     * 获取同一学校中的所有自习室
     * 该接口根据请求中的学校信息返回所有自习室
     * @param request 请求对象，包含学校信息
     * @return 返回包含所有自习室的结果
     */
    @GetMapping("/getRoomsBySchool")
    @ApiOperation(value = "获取同一学校中的所有自习室")
    public Result getRoomsBySchool(HttpServletRequest request){
        return roomService.getRoomsBySchool(request);
    }

    /**
     * 根据状态获取可用或不可用的自习室
     * 该接口根据自习室的状态（可用或不可用）返回对应的自习室列表
     * @param status 自习室的状态（0 - 不可用, 1 - 可用）
     * @return 返回符合状态条件的自习室列表
     */
    @RequiresAuthentication
    @GetMapping("/getRoomsByStatus/{status}")
    @ApiOperation(value = "根据状态获取可用或不可用的自习室接口")
    public Result getRoomsByStatus(@PathVariable(name = "status") Integer status){
        return roomService.getRoomsByStatus(status);
    }
    /**
     * 占用自习室
     * 该接口用于占用指定的自习室，设置开始和结束时间
     * @param roomVO 包含自习室信息的对象，包括开始时间和结束时间
     * @return 返回占用自习室操作的结果
     */
    @RequiresAuthentication
    @PostMapping("/Occupy")
    @ApiOperation(value = "占用自习室接口")
    public Result Occupy(RoomVO roomVO){
        Room room = new Room();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime starttime = LocalDateTime.parse(roomVO.getStarttime(), df);
        LocalDateTime endtime = LocalDateTime.parse(roomVO.getEndtime(), df);
        //设置自习室的占用信息
        room.setId(roomVO.getId());
        room.setStatus(roomVO.getStatus());
        room.setSchool(roomVO.getSchool());
        room.setStarttime(starttime);
        room.setEndtime(endtime);
        //调用服务层方法进行自习室占用
        return roomService.Occupy(room);
    }

}

