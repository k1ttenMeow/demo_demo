package com.followup.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String username;
    private String realName;
    private Integer userType;
    private String phone;
    private String idCard;
    private String address;
    private String department;
    private String title;
    private LocalDateTime createTime;
}