package com.jason.classroom.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.*;
import com.jason.classroom.mapper.*;
import com.jason.classroom.service.SignService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  服务实现类，处理签到相关操作
 * </p>
 */
@Service
public class SignServiceImpl extends ServiceImpl<SignMapper, Sign> implements SignService {

    @Resource
    private SignMapper signMapper; // 签到数据访问接口
    @Resource
    private TableMapper tableMapper;//桌子数据访问接口——更新桌子可用状态
    @Resource
    private OrderMapper orderMapper;//订单访问接口——访问最近的一次以便获取教室和桌号
    @Resource
    private ViolationMapper violationMapper;//违规数据接口
    @Resource
    private UserMapper userMapper;//用户数据接口


    /**
     * 用户签到操作
     *
     * @param request HttpServletRequest对象，获取当前用户的会话信息
     * @return Result 签到结果
     */
    @Override
    public Result signIn(HttpServletRequest request) {
        // 获取当前时间
        Date currentDate = new Date();
        String currentTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentDate);
        // 获取当前用户
        User user = (User) request.getSession().getAttribute("user");
        User user1 = userMapper.
                selectByUsername(user.getUsername());

        // 从请求参数中获取 action（判断是预览还是实际签到）
        String action = request.getParameter("action");

        // 1. 检查用户是否有最近的预约记录
        Order nearestOrder = orderMapper.selectOne(new QueryWrapper<Order>()
                .eq("username", user.getUsername())
                .eq("status", 0) // 未签到的
                .orderByDesc("starttime")  // 按开始时间降序
                .last("LIMIT 1"));  // 取最近的一条

        if (nearestOrder == null) {
            return Result.fail("没有找到有效的预约记录，无法签到");
        }

        // 2. 检查是否在允许签到的时间范围内
        LocalDateTime orderStartTime = nearestOrder.getStarttime();
        LocalDateTime currentTime = currentDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // 计算允许签到的时间窗口
        LocalDateTime earliestSignInTime = orderStartTime.minusMinutes(10);
        int points = 0;
        String detail = "";
        boolean isViolation = false;

        // 在预约前十分钟可以提前签到
        if (currentTime.isBefore(earliestSignInTime)) {
            return Result.fail("签到时间未到，请在预约开始前10分钟内签到");
        }

        // 计算迟到时间和扣分
        if (currentTime.isAfter(orderStartTime.plusMinutes(10)) && currentTime.isBefore(orderStartTime.plusMinutes(20))) {
            // 迟到10-20分钟 扣一分
            points = 1;
            isViolation = true;
        } else if (currentTime.isAfter(orderStartTime.plusMinutes(20)) && currentTime.isBefore(orderStartTime.plusMinutes(30))) {
            // 迟到20-30分钟 扣2分
            points = 2;
            isViolation = true;
        } else if (currentTime.isAfter(orderStartTime.plusMinutes(30))) {
            // 迟到30分钟 扣3分
            points = 3;
            isViolation = true;
        }

        // 生成扣分详情
        if (isViolation) {
            long minutesLate = Duration.between(orderStartTime, currentTime).toMinutes();
            detail = String.format(
                    "%s 签到，迟到 %d 分钟，预约签到时间为 %s，按照规定扣除信誉分 %d 分，当前信誉分：%d，扣除后信誉分：%d",
                    currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    minutesLate,
                    orderStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    points,
                    user1.getPoints(),
                    user1.getPoints()-points
            );
        } else {
            detail = String.format(
                    "%s 签到，签到时间在正常范围内（预约签到时间为 %s），无需扣分",
                    currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    orderStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
        }

        // 如果是预览请求，只返回详情，不执行实际签到
        if ("preview".equals(action)) {
            Map<String, Object> data = new HashMap<>();
            data.put("detail", detail);
            data.put("points", points);
            data.put("isViolation", isViolation);
            return Result.succ(data);
        }

        // 实际签到逻辑
        if (isViolation) {
            // 记录违规信息
            Violation violation = new Violation();
            violation.setNum(nearestOrder.getNum());
            violation.setName(nearestOrder.getUsername());
            violation.setType(3); // 3表示迟到违规
            violation.setViolationTime(currentTime);
            violation.setDetil(detail);
            violationMapper.insert(violation);

            // 扣除用户信誉分
            user1.setPoints(user1.getPoints() - points);
            userMapper.updateById(user1);
        }

        // 3. 更新座位状态
        String tableNum = nearestOrder.getTablenum();
        String room = nearestOrder.getRoom();
        String[] parts = tableNum.split("排");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1].replace("列", ""));

        tableMapper.update(null,
                new UpdateWrapper<Table>()
                        .eq("room", room)
                        .eq("x", x)
                        .eq("y", y)
                        .set("status", 0)
        );

        // 4. 处理签到记录
        Sign signRecord = signMapper.selectOne(new QueryWrapper<Sign>().eq("username", user.getUsername()));

        if (signRecord != null && signRecord.getSignined() == 1) {
            return Result.succ(201, "今天已经签到过了", null);
        }

        if (signRecord != null) {
            signRecord.setSignined(1);
            signRecord.setSignouted(0);
            signRecord.setSigncount(signRecord.getSigncount() + 1);
            signRecord.setSignin(currentDate);
            signMapper.updateById(signRecord);
        } else {
            Sign newSign = new Sign();
            newSign.setUsername(user.getUsername());
            newSign.setSignined(1);
            newSign.setSignouted(0);
            newSign.setSigncount(1);
            newSign.setSignin(currentDate);
            signMapper.insert(newSign);
        }

        // 更新订单状态
        nearestOrder.setStatus(1);
        orderMapper.updateById(nearestOrder);
        userMapper.updateById(user1);
        // 返回成功响应
        return Result.succ(200, "签到成功", MapUtil.builder()
                .put("count", signRecord != null ? signRecord.getSigncount() : 1)
                .put("signined", 1)
                .put("signinTime", currentTimeStr)
                .put("detail", detail)
                .put("pointsDeducted", points)
                .map()
        );
    }

    /*
     * 用户签退操作
     * */
    @Override
    public Result signOut(HttpServletRequest request) {
        // 获取当前时间
        Date currentDate = new Date();
        String currentTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentDate);
        // 获取当前用户
        User user = (User) request.getSession().getAttribute("user");
        User user1 = userMapper.
                selectByUsername(user.getUsername());

        // 从请求参数中获取 action（判断是预览还是实际签退）
        String action = request.getParameter("action");

        // 1. 检查用户是否有最近的预约记录
        Order nearestOrder = orderMapper.selectOne(new QueryWrapper<Order>()
                .eq("username", user.getUsername())
                .orderByDesc("endtime")  // 按结束时间降序
                .eq("status", 1)         // 且是自习中的记录
                .last("LIMIT 1"));      // 取最近的一条

        if (nearestOrder == null) {
            return Result.fail("没有有效的预约记录，无法签退");
        }

        // 计算时间差
        LocalDateTime orderEndTime = nearestOrder.getEndtime();
        LocalDateTime currentTime = currentDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        long minutesDifference = Duration.between(orderEndTime, currentTime).toMinutes();

        // 定义时间窗口
        LocalDateTime earliestNormal = orderEndTime.minusMinutes(10);  // 正常签退最早时间（前10分钟）
        LocalDateTime latestAllowed = orderEndTime.plusMinutes(30);    // 最晚允许签退时间（后30分钟）

        // 检查签到记录
        Sign signRecord = signMapper.selectOne(new QueryWrapper<Sign>().eq("username", user.getUsername()));
        if (signRecord == null) {
            return Result.fail("无签到记录，无法签退");
        }
        if (signRecord.getSignouted() == 1) {
            return Result.succ(201, "已经签退了", null);
        }

        int points = 0;
        String message = "签退成功";
        boolean isViolation = false;
        String detail = "";

        // 处理不同时间段的签退逻辑
        if (currentTime.isBefore(earliestNormal)) {
            // 早退处理（早于前10分钟）
            isViolation = true;
            if (currentTime.isAfter(orderEndTime.minusMinutes(20))) {
                // 早退10-20分钟
                points = 1;
            } else if (currentTime.isAfter(orderEndTime.minusMinutes(30))) {
                // 早退20-30分钟
                points = 2;
            } else {
                // 早退超过30分钟
                points = 3;
            }
            String actionType = "提前签退";
            detail = String.format(
                    "%s 签退，%s %d 分钟，预约签退时间为 %s，按照规定扣除信誉分 %d 分,当前信誉分：%d,扣除后信誉分:%d",
                    currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    actionType,
                    Math.abs(minutesDifference),
                    orderEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    points,
                    user1.getPoints(),
                    user1.getPoints()-points
            );
            message = "签退成功，因早退扣除" + points + "分信誉分";
        } else if (currentTime.isAfter(orderEndTime) && currentTime.isBefore(latestAllowed)) {
            // 迟退处理（后30分钟内）
            isViolation = true;
            points = 1;  // 迟退统一扣1分
            String actionType = "延迟签退";
            detail = String.format(
                    "%s 签退，%s %d 分钟，预约签退时间为 %s，按照规定扣除信誉分 %d 分",
                    currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    actionType,
                    Math.abs(minutesDifference),
                    orderEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    points
            );
            message = "签退成功，因迟退扣除1分信誉分";
        } else if (currentTime.isAfter(latestAllowed)) {
            // 超过最晚允许签退时间
            return Result.fail("签退已超时，无法签退");
        } else {
            // 正常签退
            detail = String.format(
                    "%s 签退，签退时间在正常范围内（预约签退时间为 %s），无需扣分",
                    currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    orderEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
        }

        // 如果是预览请求，只返回详情，不执行实际签退
        if ("preview".equals(action)) {
            Map<String, Object> data = new HashMap<>();
            data.put("detail", detail);
            data.put("points", points);
            data.put("isViolation", isViolation);
            return Result.succ(data);
        }

        // 实际签退逻辑
        if (isViolation) {
            Violation violation = new Violation();
            violation.setNum(nearestOrder.getNum());
            violation.setName(nearestOrder.getUsername());
            violation.setType(currentTime.isBefore(orderEndTime) ? 4 : 5); // 4:早退 5:迟退
            violation.setViolationTime(currentTime);
            violation.setDetil(detail);
            violationMapper.insert(violation);

            // 扣除用户信誉分
            user1.setPoints(user1.getPoints() - points);
            userMapper.updateById(user1);
        }

        // 更新签退状态
        signRecord.setSignouted(1);
        signRecord.setSignined(0);
        signRecord.setSignout(currentDate);
        signMapper.updateById(signRecord);

        // 恢复座位状态
        String tableNum = nearestOrder.getTablenum();
        String room = nearestOrder.getRoom();
        String[] parts = tableNum.split("排");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1].replace("列", ""));

        tableMapper.update(null,
                new UpdateWrapper<Table>()
                        .eq("room", room)
                        .eq("x", x)
                        .eq("y", y)
                        .set("status", 1)
        );

        // 更新订单状态
        nearestOrder.setStatus(2);
        orderMapper.updateById(nearestOrder);

        return Result.succ(200, message, MapUtil.builder()
                .put("signOuted", 1)
                .put("signoutTime", currentTimeStr)
                .put("pointsDeducted", points)
                .put("detail", detail)
                .map()
        );
    }

    /**
     * 获取用户的签到统计信息
     * @param request HttpServletRequest对象，获取当前用户的会话信息
     * @return Result 用户签到统计信息
     */
    @Override
    public Result signCount(HttpServletRequest request) {
        // 获取当前用户信息
        User user = (User) request.getSession().getAttribute("user");

        // 查询用户的签到记录
        Sign selectOne = signMapper.selectOne(new QueryWrapper<Sign>().eq("username", user.getUsername()));

        //当前签到时间
        var signInTime = new Date().toLocaleString();
        // 如果用户有签到记录，返回签到次数和状态
        if (selectOne != null) {
            return Result.succ(MapUtil.builder()
                    .put("count", selectOne.getSigncount())
                    .put("signined", selectOne.getSignined())
                    .put("signin",signInTime)
                    .map()
            );
        } else {
            // 如果没有签到记录，返回无签到信息
            return Result.succ(400, "无签到信息", null);
        }
    }

    /**
     * 更新所有未签到用户的签到状态
     * 该方法会将所有尚未签到的用户的签到状态更新（此方法具体实现不清楚，需要补充具体的更新逻辑）
     */
    @Override
    public void updateSignTask() {
        // 更新所有未签到的用户的签到状态
        this.update(new QueryWrapper<Sign>().eq("signined", 0));
    }
}

