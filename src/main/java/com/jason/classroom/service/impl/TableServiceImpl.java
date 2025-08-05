package com.jason.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.common.vo.TableVO;
import com.jason.classroom.entity.Room;
import com.jason.classroom.entity.Table;
import com.jason.classroom.mapper.RoomMapper;
import com.jason.classroom.mapper.TableMapper;
import com.jason.classroom.service.TableService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 */@Service
public class TableServiceImpl extends ServiceImpl<TableMapper, Table> implements TableService {

    @Resource
    private TableMapper tableMapper; // 自习桌数据访问接口
    @Resource
    private RoomMapper roomMapper; // 房间数据访问接口

    /**
     * 获取某个房间的所有桌子
     *
     * @param room 房间编号
     * @return Result 包含该房间所有桌子的列表
     */
    @Override
    public Result table4List(String room) {
        // 获取房间的所有桌子信息
        List<Table> tables = tableMapper.selectList(new QueryWrapper<Table>().eq("room", room));
        return Result.succ(tables); // 返回成功的结果，包含桌子列表
    }

    /**
     * 预定某张桌子
     *
     * @param table 预定的桌子信息
     * @return Result 预定结果
     */
    @Override
    public Result orderTable(Table table) {
        // 检查该自习室是否被整体占用
        Room room = roomMapper.selectOne(new QueryWrapper<Room>().eq("num", table.getRoom()));
        // 如果房间的状态不是可用（status != 1），表示该房间已经被预定
        if (room.getStatus() != 1){
            return Result.fail("该自习室已被预定");
        } else {
            // 查找指定桌子
            Table select = tableMapper.selectById(table.getId());
            // 如果桌子的状态是可预定（status == 1），则将其状态设置为已预定（status = 0）
            if (select.getStatus() == 1){
                table.setStatus(0); // 设置桌子为已预定状态
                tableMapper.updateById(table); // 更新数据库中的桌子记录
                return Result.succ("预定成功"); // 返回预定成功的结果
            }
            // 如果桌子状态不是可预定，返回失败信息
            return Result.fail("不可预定");
        }
    }

    /**
     * 获取可以预定的桌子
     *
     * @param tablenum 房间号
     * @return Result 包含所有可以预定的桌子的列表
     */
    @Override
    public Result getAbleTable(String tablenum) {
        // 获取房间中所有桌子
        List<Table> tables = tableMapper.selectList(new QueryWrapper<Table>().eq("room", tablenum));
        // 存储所有可预定桌子的位置（坐标）
        ArrayList<String> list = new ArrayList<>();
        tables.forEach(item -> {
            list.add(item.getX() + "；" + item.getY()); // 获取桌子的坐标并加入列表
        });
        return Result.succ(list); // 返回成功的结果，包含桌子坐标列表
    }

    /*
    * 修改座位状态
    * */
    @Override
    public Result updateTable(TableVO tableVO) {
        try {
            Table table = new Table();
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // 转换时间格式
            if (tableVO.getStarttime() != null && !tableVO.getStarttime().isEmpty()) {
                LocalDateTime starttime = LocalDateTime.parse(tableVO.getStarttime(), df);
                table.setStarttime(starttime);
            }
            if (tableVO.getEndtime() != null && !tableVO.getEndtime().isEmpty()) {
                LocalDateTime endtime = LocalDateTime.parse(tableVO.getEndtime(), df);
                table.setEndtime(endtime);
            }

            // 设置座位信息
            table.setId(tableVO.getId());
            table.setStatus(tableVO.getStatus());
            table.setRoom(tableVO.getRoom());
            table.setX(tableVO.getX());
            table.setY(tableVO.getY());

            // 检查座位是否存在
            Table existingTable = tableMapper.selectById(table.getId());
            if (existingTable == null) {
                return Result.fail("座位不存在");
            }

            // 更新座位信息
            int result = tableMapper.updateById(table);
            if (result > 0) {
                return Result.succ("座位状态更新成功");
            } else {
                return Result.fail("座位状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新座位状态出错", e);
            return Result.fail("更新座位状态时发生错误: " + e.getMessage());
        }
    }

}

