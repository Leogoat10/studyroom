package com.jason.classroom.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jason.classroom.controller.RoomController;
import com.jason.classroom.entity.Course;
import com.jason.classroom.entity.Room;
import com.jason.classroom.mapper.CourseMapper;
import com.jason.classroom.mapper.RoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoomStatusScheduler {
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private RoomMapper roomMapper;

    @Scheduled(fixedRate = 30000)
    public void updateRoomTask() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 先处理已结束的课程，但只处理那些没有正在进行的课程的教室
        List<Course> finishedCourses = courseMapper.selectList(
                new QueryWrapper<Course>()
                        .lt("endtime", now)
        );

        for (Course course : finishedCourses) {
            // 检查该教室是否有正在进行的课程
            Long ongoingCount = Long.valueOf(courseMapper.selectCount(
                    new QueryWrapper<Course>()
                            .eq("room", course.getRoom())
                            .le("starttime", now)
                            .ge("endtime", now)
            ));

            if (ongoingCount == 0) {
                roomMapper.update(null,
                        new UpdateWrapper<Room>()
                                .eq("num", course.getRoom())
                                .set("status", 1)
                );
            }
        }

        // 2. 处理正在进行的课程（优先级高）
        List<Course> ongoingCourses = courseMapper.selectList(
                new QueryWrapper<Course>()
                        .le("starttime", now)
                        .ge("endtime", now)
        );
        for (Course course : ongoingCourses) {
            roomMapper.update(null,
                    new UpdateWrapper<Room>()
                            .eq("num", course.getRoom())
                            .set("status", 0)
            );
        }

/*      System.out.println("定时任务执行中：" + LocalDateTime.now());
        System.out.println("当前时间：" + now);
        System.out.println("进行中课程数：" + ongoingCourses.size());
        System.out.println("已结束课程数：" + finishedCourses.size());*/
    }
}