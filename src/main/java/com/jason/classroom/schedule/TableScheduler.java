package com.jason.classroom.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jason.classroom.entity.Order;
import com.jason.classroom.entity.Table;
import com.jason.classroom.entity.User;
import com.jason.classroom.entity.Violation;
import com.jason.classroom.mapper.OrderMapper;
import com.jason.classroom.mapper.TableMapper;
import com.jason.classroom.mapper.UserMapper;
import com.jason.classroom.mapper.ViolationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

//座位定时器——管理员
@Component
public class TableScheduler {
    @Autowired
    private TableMapper tableMapper;

    @Scheduled(fixedRate = 30000)
    public void updateTableTask() {
        LocalDateTime now = LocalDateTime.now();
        // 获取禁用到时的座位
        List<Table> origintables = tableMapper.selectList(
                new QueryWrapper<Table>()
                        .le("endtime", now)//当前时间在endtime之后
                        .eq("status",2)//被管理员设置为不可用
        );
        for (Table table : origintables) {
            //更新座位状态
            tableMapper.update(null,
                    new UpdateWrapper<Table>()
                            .eq("id", table.getId())//获取座位编号
                            .set("status", 1)//到时后设置可用
            );
        }
    }
}
