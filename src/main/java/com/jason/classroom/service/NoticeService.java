package com.jason.classroom.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.Notice;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface NoticeService extends IService<Notice> {
    public Map<String,Object> noticeList();

    public Result Delete(Notice notice);

    public Result Add(Notice notice);
    public Result update(Notice notice);
}
