package com.jason.classroom.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NoticeVo {
    private String title;
    private int type;
    private String content;
    private String starttime;
    private String endtime;
    private String createtime;
}
