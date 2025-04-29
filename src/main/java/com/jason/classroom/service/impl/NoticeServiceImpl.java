package com.jason.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.Course;
import com.jason.classroom.entity.Notice;
import com.jason.classroom.entity.Teacher;
import com.jason.classroom.mapper.NoticeMapper;
import com.jason.classroom.service.NoticeService;
import org.apache.commons.lang3.StringUtils;
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

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {
    @Resource
    private NoticeMapper noticeMapper;
    /*
    * 获取通知列表
    *
    * */
    @Override
    public Map<String, Object> noticeList() {
        Map<String,Object> map = new HashMap<>();
        List<Map<String, Object>> notices = noticeMapper.selectMaps(new QueryWrapper<Notice>());
        //格式化时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Map<String, Object> notice : notices) {//遍历取到的通知信息
            Object startObj = notice.get("starttime");
            Object endObj = notice.get("endtime");
            Object createObj = notice.get("createtime");
            if (startObj instanceof Timestamp) {
                LocalDateTime start = ((Timestamp) startObj).toLocalDateTime();
                notice.put("starttime", formatter.format(start));
            }
            if (endObj instanceof Timestamp) {
                LocalDateTime end = ((Timestamp) endObj).toLocalDateTime();
                notice.put("endtime", formatter.format(end));
            }
            if (createObj instanceof Timestamp) {
                LocalDateTime createtime = ((Timestamp) createObj).toLocalDateTime();
                notice.put("createtime", formatter.format(createtime));
            }
        }
        map.put("data", notices);
        map.put("code", 0);
        map.put("msg", "获取数据成功");
        return map;
    }
    /*
     * 删除通知
     * */
    @Override
    public Result Delete(Notice notice) {
        // 1. 检查通知是否存在
        Notice selectOne = noticeMapper.selectById(notice.getId());
        if (selectOne == null) {
            return Result.fail("通知不存在或已被删除");
        }
        // 2. 执行删除操作
        int deleteCount = noticeMapper.deleteById(notice.getId());
        // 3. 返回操作结果
        if (deleteCount > 0) {
            return Result.succ("删除通知成功");
        } else {
            return Result.fail("删除通知失败");
        }
    }

    @Override
    public Result Add(Notice notice) {
        try {
            // 1. 参数校验
            if (StringUtils.isBlank(notice.getTitle())) {
                return Result.fail("通知标题不能为空");
            }
            if (notice.getType() <= 0) {
                return Result.fail("请选择有效的通知类型");
            }
            if (StringUtils.isBlank(notice.getContent())) {
                return Result.fail("通知内容不能为空");
            }
            if (notice.getStarttime() == null) {
                return Result.fail("请设置生效时间");
            }

            // 2. 校验时间逻辑
            if (notice.getEndtime() != null && notice.getEndtime().isBefore(notice.getStarttime())) {
                return Result.fail("过期时间不能早于生效时间");
            }

            // 3. 设置默认值
            notice.setCreatetime(LocalDateTime.now());
            // 4. 保存到数据库
            boolean success = save(notice);

            if (success) {
                return Result.succ("通知发布成功");
            } else {
                return Result.fail("通知发布失败");
            }
        } catch (Exception e) {
            log.error("发布通知失败", e);
            return Result.fail("系统错误，发布通知失败");
        }
    }
    /*
    * 修改编辑通知
    * */
    @Override
    public Result update(Notice notice) {
        try {
            // 1. 参数校验
            if (notice.getId() == null || notice.getId() <= 0) {
                return Result.fail("通知ID不能为空");
            }
            if (StringUtils.isBlank(notice.getTitle())) {
                return Result.fail("通知标题不能为空");
            }
            if (notice.getType() <= 0) {
                return Result.fail("请选择有效的通知类型");
            }
            if (StringUtils.isBlank(notice.getContent())) {
                return Result.fail("通知内容不能为空");
            }
            if (notice.getStarttime() == null) {
                return Result.fail("请设置生效时间");
            }

            // 2. 校验时间逻辑
            if (notice.getEndtime() != null && notice.getEndtime().isBefore(notice.getStarttime())) {
                return Result.fail("过期时间不能早于生效时间");
            }

            // 3. 检查通知是否存在
            Notice existingNotice = getById(notice.getId());
            if (existingNotice == null) {
                return Result.fail("未找到要更新的通知");
            }

            // 4. 更新通知信息（不修改创建时间）
            notice.setCreatetime(existingNotice.getCreatetime());

            // 5. 执行更新
            boolean success = updateById(notice);

            if (success) {
                return Result.succ("通知更新成功");
            } else {
                return Result.fail("通知更新失败");
            }
        } catch (Exception e) {
            log.error("更新通知失败", e);
            return Result.fail("系统错误，更新通知失败");
        }
    }

}

