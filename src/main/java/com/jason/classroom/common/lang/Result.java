package com.jason.classroom.common.lang;

import lombok.Data;

import java.io.Serializable;

@Data // Lombok 注解，自动生成 getter、setter、toString、equals 和 hashCode 方法
public class Result implements Serializable {
    private int code;   // 状态码
    private String msg;  // 消息
    private Object data; // 数据

    // 返回成功的结果，默认状态码为 200，消息为"操作成功"
    public static Result succ(Object data){
        return succ(200,"操作成功",data);
    }

    // 返回成功的结果，允许传递状态码、消息和数据
    public static Result succ(int code, String msg, Object data){
        Result r = new Result();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    // 返回成功的结果，默认状态码为 200，传递消息
    public static Result succ(String msg){
        return succ(200,msg,null);
    }

    // 返回失败的结果，默认状态码为 400，传递消息
    public static Result fail(String msg){
        return fail(400,msg,null);
    }

    // 返回失败的结果，传递消息和数据
    public static Result fail(String msg, Object data){
        return fail(400,msg,data);
    }

    // 返回失败的结果，允许传递状态码、消息和数据
    public static Result fail(int code, String msg, Object data){
        Result r = new Result();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}

