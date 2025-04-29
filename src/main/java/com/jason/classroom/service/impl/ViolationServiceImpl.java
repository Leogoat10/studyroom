package com.jason.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.common.vo.ViolationVO;
import com.jason.classroom.entity.Notice;
import com.jason.classroom.entity.Order;
import com.jason.classroom.entity.User;
import com.jason.classroom.entity.Violation;
import com.jason.classroom.mapper.NoticeMapper;
import com.jason.classroom.mapper.ViolationMapper;
import com.jason.classroom.service.NoticeService;
import com.jason.classroom.service.ViolationService;
import lombok.extern.slf4j.Slf4j;
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
/*
* 记录违规+扣分
* */
@Service
@Slf4j
public class ViolationServiceImpl extends ServiceImpl<ViolationMapper, Violation> implements ViolationService {
    @Resource
    private ViolationMapper violationMapper;
    //获取违规记录表
    @Override
    public Map<String, Object> violationList() {
        Map<String,Object> map = new HashMap<>();
        List<Map<String, Object>> violations = violationMapper.selectMaps(new QueryWrapper<Violation>());
        //格式化时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Map<String, Object> violation : violations) {//遍历取到的通知信息
            Object violationTime = violation.get("violationTime");
            if (violationTime instanceof Timestamp) {
                LocalDateTime violationTime1 = ((Timestamp) violationTime).toLocalDateTime();
                violation.put("violationTime", formatter.format(violationTime1));
            }
        }
        map.put("data", violations);
        map.put("code", 0);
        map.put("msg", "获取数据成功");
        return map;
    }

    /*
    * 学生查看违规记录
    * */
    @Override
    public Map<String, Object> getViolationByUsername(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();

        // 获取当前用户信息
        User user = (User) request.getSession().getAttribute("user");
        // 定义格式化器
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 根据用户名查询用户的所有租用
        List<Map<String, Object>> violations = violationMapper.selectMaps(new QueryWrapper<Violation>().eq("name", user.getUsername()));
        List<Map<String, Object>> maps = new ArrayList<>();

        // 遍历租用数据，格式化租用的时间信息
        violations.forEach(item -> {
            Object violationTime = item.get("violationTime");
            if (violationTime instanceof Timestamp) {
                LocalDateTime violationTime1 = ((Timestamp) violationTime).toLocalDateTime();
                item.put("violationTime", dateFormatter.format(violationTime1));
            }
            // 构建违约信息的Map
            HashMap<String, Object> vo = new HashMap<>();
            vo.put("id", item.get("id"));
            vo.put("violationTime", item.get("violationTime"));
            vo.put("num", item.get("num"));
            vo.put("name", item.get("name"));
            vo.put("type", item.get("type"));
            vo.put("detil", item.get("detil"));
            vo.put("feedback", item.get("feedback"));
            vo.put("dispose", item.get("dispose"));
            vo.put("feedbackStatus", item.get("feedbackStatus"));
            maps.add(vo);
        });
        // 返回封装好的租用数据
        map.put("data", maps);
        map.put("code", 0);
        map.put("msg", "获取数据成功");
        return map;
    }
    /*
    * 学生反馈违规行为
    * */
    @Override
    public Result feedback(ViolationVO violationVO, HttpServletRequest request) {
        Violation violation = violationMapper.selectById(violationVO.getId());

        violation.setFeedback(violationVO.getFeedback());

        // 设置租用的其他信息
        violation.setFeedbackStatus(1);//状态改为待解决
        // 更新到数据库
        violationMapper.updateById(violation);
        return Result.succ("反馈成功");
    }
    /*
    * 老师处理反馈记录
    * */
    @Override
    public Result dispose(ViolationVO violationVO, HttpServletRequest request) {
        Violation violation = violationMapper.selectById(violationVO.getId());
        //反馈结果
        violation.setDispose(violationVO.getDispose());
        //反馈后违规状态
        violation.setType(violationVO.getType());

        // 设置其他信息
        violation.setFeedbackStatus(2);//状态改为已解决
        // 更新到数据库
        violationMapper.updateById(violation);
        return Result.succ("处理成功");
    }
}
