package com.jason.classroom.schedule;

import com.jason.classroom.controller.SignController;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import javax.annotation.Resource;
@Configuration // 声明这是一个配置类，Spring会加载此类的配置
@EnableScheduling // 启用Spring的定时任务功能
public class UpdateScheduleTask {

    @Resource
    private SignController signController; // 注入SignController，用于调用其中的方法
    /**
     * 定时更新签到任务的方法
     *
     * @Scheduled注解表示这是一个定时任务
     * cron = "0 0 0 * * ?" 表示每天的00:00:00触发一次
     */
    @Scheduled(cron = "0 0 0 * * ?")
    private void updateSignTask() {
        signController.updateSignTask(); // 调用SignController中的updateSignTask方法
    }
}
