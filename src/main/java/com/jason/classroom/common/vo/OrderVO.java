package com.jason.classroom.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderVO {
    private String num;

    private String room;

    private String x;

    private String y;

    private String starttime;

    private String endtime;

    private String date;

    private int status;
}
