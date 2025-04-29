package com.jason.classroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_violation")
public class Violation implements Serializable {//
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)

    private int id;
    private String num;
    private int type;
    private String detil;
    private String name;
    @TableField("violationTime")
    private LocalDateTime violationTime;
    private String feedback;
    @TableField("feedbackStatus")
    private int feedbackStatus;
    private String dispose;
}

