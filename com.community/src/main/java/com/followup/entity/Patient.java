package com.followup.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 患者表 实体
 *
 * @author system
 * @date 2026-03-23
 * @table patient
 */
@Data
@TableName("patient")
public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID（关联 user 表）
     */
    private Long userId;

    /**
     * 责任医生 ID
     */
    private Long doctorId;

    /**
     * 慢病类型
     */
    private String chronicType;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 地址
     */
    private String address;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系电话
     */
    private String emergencyPhone;

    /**
     * 患者姓名（关联查询字段，非数据库字段）
     */
    @TableField(exist = false)
    private String realName;

    /**
     * 登录账号（关联查询字段，非数据库字段）
     */
    @TableField(exist = false)
    private String username;

    /**
     * 手机号（关联查询字段，非数据库字段）
     */
    @TableField(exist = false)
    private String phone;

    /**
     * 状态（关联查询字段，非数据库字段）
     */
    @TableField(exist = false)
    private Integer status;

    /**
     * 责任医生姓名（关联查询字段，非数据库字段）
     */
    @TableField(exist = false)
    private String doctorName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
