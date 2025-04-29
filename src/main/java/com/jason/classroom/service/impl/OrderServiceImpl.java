package com.jason.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.common.vo.CourseVO;
import com.jason.classroom.common.vo.OrderVO;
import com.jason.classroom.common.vo.TableVO;
import com.jason.classroom.entity.*;
import com.jason.classroom.mapper.*;
import com.jason.classroom.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <p>
 *  服务实现类，处理租用相关操作
 * </p>
 *
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private OrderMapper orderMapper; // 租用数据访问接口
    @Resource
    private ViolationMapper violationMapper;//违规数据接口
    @Resource
    private UserMapper userMapper;//学生数据接口
    @Resource
    private CourseMapper courseMapper;
    @Autowired
    private TableMapper tableMapper;

    /**
     * 用户进行订座操作
     * @param orderVO 租用视图对象，包含租用的所有信息
     * @param request HttpServletRequest对象，获取当前用户的会话信息
     * @return Result 订座结果
     * 同时还要同步到t_table数据库  麻烦
     */
    @Override
    public Result makeOrder(OrderVO orderVO, HttpServletRequest request) {
        Order order = new Order();
        // 获取当前用户信息
        User user = (User) request.getSession().getAttribute("user");
        // 获取日期并格式化租用的开始时间和结束时间
        String date = orderVO.getDate();
        String starttimevo = orderVO.getStarttime();
        String starthour = starttimevo.substring(0, starttimevo.indexOf("点"));
        if (starthour.length() == 1) {
            starthour = "0" + starthour;
        }
        String startminute = starttimevo.substring(starttimevo.indexOf("点") + 1, starttimevo.indexOf("分"));
        if (startminute.length() == 1) {
            startminute = "0" + startminute;
        }
        String starttimest = date + " " + starthour + ":" + startminute + ":00";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String endtimevo = orderVO.getEndtime();
        String endhour = endtimevo.substring(0, endtimevo.indexOf("点"));
        if (endhour.length() == 1) {
            endhour = "0" + endhour;
        }
        String endminute = endtimevo.substring(endtimevo.indexOf("点") + 1, endtimevo.indexOf("分"));
        if (endminute.length() == 1) {
            endminute = "0" + endminute;
        }
        String endtimest = date + " " + endhour + ":" + endminute + ":00";
        // 将字符串时间转为LocalDateTime类型
        LocalDateTime starttime = LocalDateTime.parse(starttimest, fmt);
        LocalDateTime endtime = LocalDateTime.parse(endtimest, fmt);
        order.setStarttime(starttime);
        order.setEndtime(endtime);
        // 从order表中  直接检查座位是否可用
        //转换为几排几列
        String seat = orderVO.getX() + "排" + orderVO.getY() + "列";
        List<String> conflicts = getConflictTimes(orderVO.getRoom(), seat, starttime, endtime);
        if (!conflicts.isEmpty()) {
            String conflictMsg = String.join("\n", conflicts);
            return Result.fail("该座位此时段已被占用，冲突时间如下：\n" + conflictMsg);
        }
        //从table表中  检查座位是否可用
        int a = Integer.parseInt(orderVO.getX());
        int b = Integer.parseInt(orderVO.getX());
        List<String> conflicts1 = getConflictTimes1(orderVO.getRoom(), a,b,starttime, endtime);
        if (!conflicts1.isEmpty()) {
            String conflictMsg = String.join("\n", conflicts1);
            return Result.fail("该座位被管理员设为不可用，时间如下：\n" + conflictMsg);
        }
        // 从course表中  直接检查教室是否可用
        String room = orderVO.getRoom();
        List<String> conflictrooms = getConflictTimesRoom(room, starttime, endtime);
        if (!conflictrooms.isEmpty()) {
            String conflictMsg = String.join("\n", conflictrooms);
            return Result.fail("该教室此时段已被占用，冲突时间如下：\n" + conflictMsg);
        }
        //低分用户不可租用（80分以下）
        User user1 = userMapper.
                selectByUsername(user.getUsername());
        if(user1.getPoints()<80){
            return Result.fail("您的信誉分较低，不可租用座位，请联系管理员参加相关志愿者服务提升信誉分\n当前信誉分为："+user1.getPoints());
        }
        // 设置租用的其他信息
        order.setRoom(orderVO.getRoom());
        order.setTablenum(orderVO.getX() + "排" + orderVO.getY() + "列");
        order.setSchool(user.getSchool());
        order.setNum(String.valueOf(Math.round((Math.random() + 1) * 1000))); // 生成随机租用编号
        order.setUsername(user.getUsername());
        order.setStatus(0);
        // 将租用插入到数据库
        orderMapper.insert(order);
        return Result.succ("订座成功");
    }

    /*
    * 取消预约
    * */
    @Override
    public Result cancelOrder(OrderVO orderVO, HttpServletRequest request) {

        // 1. 参数校验
        if (orderVO == null || orderVO.getNum() == null) {
            return Result.fail("参数错误");
        }

        // 2. 从请求参数中获取 action（判断是预览还是实际取消）
        String action = request.getParameter("action");

        // 3. 查询订单和对应用户
        Order order = orderMapper.
                selectByNum(orderVO.getNum());
        User user = userMapper.selectByUsername(order.getUsername());
        if (order == null) {
            return Result.fail("订单不存在");
        }

        /* 计算取消详情和扣分（用于预览和实际取消） */
        LocalDateTime currentTime = LocalDateTime.now();
        // 检查订单状态是否可以取消（假设只有状态为 0 的可以取消）
        if (order.getStatus() != 0 || currentTime.isAfter(order.getStarttime())) {
            return Result.fail("当前状态不可取消");
        }

        // 计算取消详情和扣分（用于预览和实际取消）

        LocalDateTime orderStartTime = order.getStarttime();
        long minutesRemaining = Duration.between(currentTime, orderStartTime).toMinutes();

        int points = 0;
        LocalDateTime first = orderStartTime.minusMinutes(120);
        LocalDateTime second = orderStartTime.minusMinutes(60);
        LocalDateTime third = orderStartTime.minusMinutes(30);

        if (currentTime.isBefore(first)) {
            points = 1;
        } else if (currentTime.isAfter(second) && currentTime.isBefore(third)) {
            points = 2;
        } else if (currentTime.isAfter(third)) {
            points = 3;
        }

        String detail = String.format(
                "在 %s 取消预约，距离签到时间还有 %d 分钟，预约时间为 %s，按照规定扣除信誉分 %d 分，当前信誉分：%d，扣除后信誉分：%d",
                currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                minutesRemaining,
                orderStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                points,
                user.getPoints(),
                user.getPoints()-points
        );

        // 6. 如果是预览请求，只返回详情，不执行取消
        if ("preview".equals(action)) {
            Map<String, Object> data = new HashMap<>();
            data.put("detail", detail);
            data.put("points", points);
            return Result.succ(data);
        }

        // 7. 实际取消逻辑
        order.setStatus(3); // 3 表示已取消

        // 8. 记录违规信息
        Violation violation = new Violation();
        violation.setNum(order.getNum());
        violation.setName(order.getUsername());
        violation.setType(1); // 1 表示取消预约违规
        violation.setViolationTime(currentTime);
        violation.setDetil(detail);
        violationMapper.insert(violation);

        // 9. 扣除用户信誉分
        userMapper.update(
                null,
                new UpdateWrapper<User>()
                        .eq("username", order.getUsername())
                        .set("points", user.getPoints() - points)
        );

        // 10. 更新订单状态
        int result = orderMapper.updateById(order);
        if (result <= 0) {
            return Result.fail("取消失败");
        }
        // 11. 返回取消成功信息（包含扣分详情）
        Map<String, Object> data = new HashMap<>();
        data.put("detail", detail);
        data.put("points", points);
        return Result.succ(data);
    }

    /*
     * 检查指定座位在指定时间段是否可用——从订单方面
     */
    private List<String> getConflictTimes(String roomnum, String tablenum, LocalDateTime starttime, LocalDateTime endtime) {
        List<String> conflictTimes = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Order> orders = orderMapper.selectList(
                new QueryWrapper<Order>()
                        .eq("room", roomnum)
                        .in("status", Arrays.asList(0, 1))  // 0被预约但还没签到 或 1开始自习中
                        .eq("tablenum", tablenum)
        );
        for (Order order : orders) {
            LocalDateTime starttime1 = order.getStarttime();
            LocalDateTime endtime1 = order.getEndtime();
            if (starttime1 != null && endtime1 != null) {
                boolean overlap = !(endtime.isBefore(starttime1) || starttime.isAfter(endtime1));
                if (overlap) {
                    String conflict = fmt.format(starttime1) + " - " + fmt.format(endtime1);
                    conflictTimes.add(conflict);
                }
            }
        }
        return conflictTimes;
    }
    /*
     * 检查指定座位在指定时间段是否可用——从管理员设置方面
     */
    private List<String> getConflictTimes1(String roomnum, int a,int b, LocalDateTime starttime, LocalDateTime endtime) {
        List<String> conflictTimes = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Table> tables = tableMapper.selectList(
                new QueryWrapper<Table>()
                        .eq("room", roomnum)
                        .eq("status",2)  // 不可用的座位
                        .eq("x",a)
                        .eq("y",b)
        );
        for (Table table : tables) {
            LocalDateTime starttime1 = table.getStarttime();
            LocalDateTime endtime1 = table.getEndtime();
            if (starttime1 != null && endtime1 != null) {
                boolean overlap = !(endtime.isBefore(starttime1) || starttime.isAfter(endtime1));
                if (overlap) {
                    String conflict = fmt.format(starttime1) + " - " + fmt.format(endtime1);
                    conflictTimes.add(conflict);
                }
            }
        }
        return conflictTimes;
    }

    /*
     * 检查指定教室在指定时间段是否可用
     */
    private List<String> getConflictTimesRoom(String room, LocalDateTime starttime, LocalDateTime endtime) {
        List<String> conflictTimes = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Course> courses = courseMapper.selectList(
                new QueryWrapper<Course>()
                        .eq("room", room)
        );
        for (Course course : courses) {
            LocalDateTime starttime1 = course.getStarttime();
            LocalDateTime endtime1 = course.getEndtime();
            if (starttime1 != null && endtime1 != null) {
                boolean overlap = !(endtime.isBefore(starttime1) || starttime.isAfter(endtime1));
                if (overlap) {
                    String conflict = fmt.format(starttime1) + " - " + fmt.format(endtime1);
                    conflictTimes.add(conflict);
                }
            }
        }
        return conflictTimes;
    }
    /**
     * 获取所有租用的列表——管理员角度
     * @return Map 包含所有z租用的列表数据
     */
    @Override
    public Map<String, Object> orderList() {
        Map<String, Object> map = new HashMap<>();
        // 定义格式化器
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        // 获取所有租用
        List<Map<String, Object>> orders = orderMapper.selectMaps(new QueryWrapper<Order>());
        List<Map<String, Object>> maps = new ArrayList<>();

        // 遍历租用数据，格式化租用的时间信息
        orders.forEach(item -> {
            // 获取租用的开始时间和结束时间
            Timestamp starttimest = (Timestamp) item.get("starttime");
            LocalDateTime starttime = starttimest.toLocalDateTime();
            Timestamp endtimest = (Timestamp) item.get("endtime");
            LocalDateTime endtime = endtimest.toLocalDateTime();
            // 使用格式化器格式化时间为字符串
            String date = starttime.format(dateFormatter);
            // 构建租用信息的Map
            HashMap<String, Object> vo = new HashMap<>();
            vo.put("id",item.get("id"));
            vo.put("date", date);
            vo.put("starttime", starttime.getHour() + "时" + starttime.getMinute() + "分");
            vo.put("endtime", endtime.getHour() + "时" + endtime.getMinute() + "分");
            vo.put("num", item.get("num"));
            vo.put("room", item.get("room"));
            String tablenum = item.get("tablenum").toString();
            vo.put("tablenum", tablenum);
            vo.put("status", item.get("status"));
            maps.add(vo);
        });
        map.put("data", maps);
        map.put("code", 0);
        map.put("msg", "获取数据成功");
        return map;
    }

    /**
     * 根据用户名获取用户的所有租用
     * @param request HttpServletRequest对象，获取当前用户的会话信息
     * @return Map 包含租用数据的Map
     */
    @Override
    public Map<String, Object> getOrdersByUsername(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();

        // 获取当前用户信息
        User user = (User) request.getSession().getAttribute("user");
        // 定义格式化器
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

        // 根据用户名查询用户的所有租用
        List<Map<String, Object>> orders = orderMapper.selectMaps(new QueryWrapper<Order>().eq("username", user.getUsername()));
        List<Map<String, Object>> maps = new ArrayList<>();

        // 遍历租用数据，格式化租用的时间信息
        orders.forEach(item -> {
            // 获取租用的开始时间和结束时间
            Timestamp starttimest = (Timestamp) item.get("starttime");
            LocalDateTime starttime = starttimest.toLocalDateTime();
            Timestamp endtimest = (Timestamp) item.get("endtime");
            LocalDateTime endtime = endtimest.toLocalDateTime();
            // 使用格式化器格式化时间为字符串
            String date = starttime.format(dateFormatter);
            // 构建租用信息的Map
            HashMap<String, Object> vo = new HashMap<>();
            vo.put("id",item.get("id"));
            vo.put("date", date);
            vo.put("starttime", starttime.getHour() + "时" + starttime.getMinute() + "分");
            vo.put("endtime", endtime.getHour() + "时" + endtime.getMinute() + "分");
            vo.put("num", item.get("num"));
            vo.put("room", item.get("room"));
            String tablenum = item.get("tablenum").toString();
            vo.put("tablenum", tablenum);
            vo.put("status", item.get("status"));
            maps.add(vo);
        });
        // 返回封装好的租用数据
        map.put("data", maps);
        map.put("code", 0);
        map.put("msg", "获取数据成功");
        return map;
    }
    @Override
    public Map<String, Object> getTodayOrdersBySeat(String room, int x, int y) {
        Map<String, Object> map = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        String seat = x +"排" + y + "列";
        List<Map<String, Object>> orders = orderMapper.selectMaps(
                new QueryWrapper<Order>()
                        .eq("room", room)
                        .eq("tablenum", seat)
        );

        List<Map<String, Object>> todayOrders = new ArrayList<>();
        for (Map<String, Object> order : orders) {
            Timestamp starttimest = (Timestamp) order.get("starttime");
            LocalDateTime starttime = starttimest.toLocalDateTime();

            // 判断是否为今天或明天
            if (!(starttime.toLocalDate().equals(today) || starttime.toLocalDate().equals(tomorrow))) {
                continue;
            }

            Timestamp endtimest = (Timestamp) order.get("endtime");
            LocalDateTime endtime = endtimest.toLocalDateTime();

            Map<String, Object> vo = new HashMap<>();
            vo.put("date", starttime.format(dateFormatter));
            vo.put("starttime", starttime.getHour() + "时" + starttime.getMinute() + "分");
            vo.put("endtime", endtime.getHour() + "时" + endtime.getMinute() + "分");
            vo.put("status", order.get("status"));
            todayOrders.add(vo);
        }
        map.put("data", todayOrders);
        map.put("code", 0);
        map.put("msg", "获取数据成功");
        return map;
    }
}

