package com.jason.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.common.vo.CourseVO;
import com.jason.classroom.entity.*;
import com.jason.classroom.mapper.CourseMapper;
import com.jason.classroom.service.CourseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * <p>
 *  服务实现类，处理课程相关操作
 * </p>
 */
@Slf4j
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Resource
    private CourseMapper courseMapper; // 课程数据访问接口
    /**
     * 获取教师的课程列表
     *
     * @param request HttpServletRequest对象，获取当前教师的会话信息
     * @return Map 包含课程数据的Map
     */
    @Override
    public Map<String, Object> getCourseList(HttpServletRequest request) {
        Map<String,Object> map = new HashMap<>();
        // 获取当前教师信息
        Teacher teacher = (Teacher) request.getSession().getAttribute("teacher");
        // 根据教师用户名查询所有的课程信息
        List<Map<String, Object>> courses = courseMapper.selectMaps(new QueryWrapper<Course>().eq("teachername", teacher.getUsername()));
        List<Map<String, Object>> maps = new ArrayList<>();
        // 定义格式化器
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        // 遍历课程数据，格式化时间信息
        courses.forEach(item -> {
            // 获取课程的开始时间和结束时间
            Timestamp starttimest = (Timestamp) item.get("starttime");
            LocalDateTime starttime = starttimest.toLocalDateTime();
            Timestamp endtimest = (Timestamp) item.get("endtime");
            LocalDateTime endtime = endtimest.toLocalDateTime();
            // 使用格式化器格式化时间为字符串
            String date = starttime.format(dateFormatter);
            // 构建课程信息的Map
            HashMap<String,Object> vo = new HashMap<>();
            vo.put("date", date);
            vo.put("starttime", starttime.getHour() + "时" + starttime.getMinute() + "分");
            vo.put("endtime", endtime.getHour() + "时" + endtime.getMinute() + "分");
            vo.put("room", item.get("room"));
            vo.put("coursename", item.get("coursename"));
            vo.put("announcement", item.get("announcement"));
            maps.add(vo);
        });
        // 返回封装好的课程数据
        map.put("data", maps);
        map.put("code", 0);
        map.put("msg", "获取数据成功");
        return map;
    }

    @Override
    public Map<String, Object> getCourseListByRoomToday(String num) {
        Map<String,Object> map = new HashMap<>();
        // 获取今天的日期（不带时分秒）
        LocalDate today = LocalDate.now();
        // 查询所有该教室的课程
        List<Map<String, Object>> courses = courseMapper.selectMaps(
                new QueryWrapper<Course>().eq("room", num)
        );
        List<Map<String, Object>> maps = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

        courses.forEach(item -> {
            Timestamp starttimest = (Timestamp) item.get("starttime");
            LocalDateTime starttime = starttimest.toLocalDateTime();

            // 只保留今天的课程
            if (!starttime.toLocalDate().equals(today)) return;

            Timestamp endtimest = (Timestamp) item.get("endtime");
            LocalDateTime endtime = endtimest.toLocalDateTime();

            HashMap<String,Object> vo = new HashMap<>();
            vo.put("date", starttime.format(dateFormatter));
            vo.put("starttime", starttime.getHour() + "时" + starttime.getMinute() + "分");
            vo.put("endtime", endtime.getHour() + "时" + endtime.getMinute() + "分");
            vo.put("room", item.get("room"));
            vo.put("coursename", item.get("coursename"));
            vo.put("announcement", item.get("announcement"));
            maps.add(vo);
        });

        map.put("data", maps);
        map.put("code", 0);
        map.put("msg", "获取数据成功");
        return map;
    }



    /**
     * 开始一门课程，设置课程时间，教室等信息
     *
     * @param courseVO 课程视图对象，包含课程的所有信息
     * @param request HttpServletRequest对象，获取当前教师的会话信息
     * @return Result 操作结果
     */
    @Override
    public Result startCourse(CourseVO courseVO, HttpServletRequest request) {

        Course course = new Course();
        // 获取当前教师信息
        Teacher teacher = (Teacher) request.getSession().getAttribute("teacher");
        // 获取日期并格式化课程的开始时间和结束时间
        String date = courseVO.getDate();
        String starttimevo = courseVO.getStarttime();
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
        String endtimevo = courseVO.getEndtime();
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
        course.setStarttime(starttime);
        course.setEndtime(endtime);

        // 检查教室当前时段是否冲突
        List<String> conflicts = getConflictTimes(courseVO.getRoom(), starttime, endtime);
        if (!conflicts.isEmpty()) {
            String conflictMsg = String.join("\n", conflicts);
            return Result.fail("该教室此时段已被占用，冲突时间如下：\n" + conflictMsg);
        }

        // 设置课程的其他信息
        course.setCoursename(courseVO.getCoursename());
        course.setAnnouncement(courseVO.getAnnouncement());
        course.setRoom(courseVO.getRoom());
        course.setTeachername(teacher.getUsername());

        // 将课程插入到数据库
        courseMapper.insert(course);
        return Result.succ("开课成功");
    }
    /**
     * 检查指定教室在指定时间段是否冲突
     * @param roomnum 教室编号
     * @param starttime 课程开始时间
     * @param endtime 课程结束时间
     * @return Boolean 教室是否可用
     */
    private List<String> getConflictTimes(String roomnum, LocalDateTime starttime, LocalDateTime endtime) {
        List<String> conflictTimes = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Course> courses = courseMapper.selectList(
                new QueryWrapper<Course>()
                        .eq("room", roomnum)
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
}
