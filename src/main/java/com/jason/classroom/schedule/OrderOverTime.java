package com.jason.classroom.schedule;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jason.classroom.entity.*;
import com.jason.classroom.mapper.*;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/*
* 缺勤计时器
* */
@Component
public class OrderOverTime {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ViolationMapper violationMapper;
    @Autowired
    private UserMapper userMapper;

    @Scheduled(fixedRate = 30000)
    public void updateRoomTask() {
        LocalDateTime now = LocalDateTime.now();
        // 获取超时预约的订单
        List<Order> ongoingOrders = orderMapper.selectList(
                new QueryWrapper<Order>()
                        .le("endtime", now)//当前时间在endtime之后
                        .eq("status",0)//并且未签到的预约
        );
        for (Order order : ongoingOrders) {
            //更新订单状态
            orderMapper.update(null,
                    new UpdateWrapper<Order>()
                            .eq("num", order.getNum())//获取订单编号
                            .set("status", 4)
            );
            // 获取当前时间
            Date currentDate = new Date();
            // 将Date转换为LocalTime
            LocalTime currentTime1 = currentDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime();
            LocalDateTime dateTime = currentTime1.atDate(LocalDate.now());
            Violation violation = new Violation();
            violation.setNum(order.getNum());
            violation.setName(order.getUsername());
            violation.setViolationTime(dateTime);
            violation.setType(2);//缺席
            violation.setDetil("因缺席自习室预约，扣除5分信誉分");
            violationMapper.insert(violation);
            //扣除对应用户的信誉分
            User user = userMapper.selectByUsername(order.getUsername());
            user.setPoints(user.getPoints() - 5);
            userMapper.update(null,
                    new UpdateWrapper<User>()
                            .eq("username", order.getUsername())
                            .set("points", user.getPoints() - 5)
            );
        }
    }
}