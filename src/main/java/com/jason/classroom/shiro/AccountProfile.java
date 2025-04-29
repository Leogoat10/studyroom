package com.jason.classroom.shiro;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AccountProfile implements Serializable {
    private Integer id;

    private String username;

    private String phone;

    private String email;

    private String school;
}
