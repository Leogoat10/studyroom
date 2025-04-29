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
public class ViolationVO {
    private int id;
    private String num;
    private int type;
    private String detil;
    private String name;
    private String violationTime;
    private String feedback;
    private int feedbackStatus;
    private String dispose;
}
