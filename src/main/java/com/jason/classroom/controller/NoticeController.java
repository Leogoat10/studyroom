package com.jason.classroom.controller;

import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.Notice;
import com.jason.classroom.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/notice")
@Api(tags = {"通知接口"})
public class NoticeController {
    @Autowired
    private NoticeService noticeService;
    /*
    * 获取通知列表接口
    * */
    @GetMapping("/noticelist")
    @ApiOperation(value = "获取通知列表")
    public Map<String ,Object> noticeList() {
        return noticeService.noticeList();
    }
    /*
    * 删除通知
    * */
    @PostMapping("/delete")
    @ApiOperation(value = "删除通知")
    public Result delete(Notice notice) {
        return noticeService.Delete(notice);
    }
    /*
    * 新建通知
    * */
    @PostMapping("/add")
    @ApiOperation(value = "新建通知")
    public  Result add(Notice notice) {
        return noticeService.Add(notice);
    }
    /*
    * 修改通知
    * */
    @PostMapping("/update")
    @ApiOperation(value = "修改通知")
    public Result update(Notice notice) {
        return noticeService.update(notice);
    }
}
