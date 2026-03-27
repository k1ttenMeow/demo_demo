package com.followup.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户表
 *
 * @author medical
 * @date 2026-03-26
 */
@Data
@TableName("user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 登录密码（加密存储）
     */
    private String password;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 所属科室（仅医生使用，非数据库字段）
     */
    @TableField(exist = false)
    private String department;

    /**
     * 擅长技能（仅医生使用，非数据库字段）
     */
    @TableField(exist = false)
    private String skill;

    /**
     * 所属社区（仅医生使用，非数据库字段）
     */
    @TableField(exist = false)
    private String community;

    /**
     * 慢病类型（仅患者使用，非数据库字段）
     */
    @TableField(exist = false)
    private String chronicType;

    /**
     * 年龄（仅患者使用，非数据库字段）
     */
    @TableField(exist = false)
    private Integer age;

    /**
     * 地址（仅患者使用，非数据库字段）
     */
    @TableField(exist = false)
    private String address;

    /**
     * 紧急联系人（仅患者使用，非数据库字段）
     */
    @TableField(exist = false)
    private String emergencyContact;

    /**
     * 紧急联系电话（仅患者使用，非数据库字段）
     */
    @TableField(exist = false)
    private String emergencyPhone;

    /**
     * 用户类型（1-管理员 2-医生 3-患者）
     */
    @TableField("role")
    private Integer userType;

    /**
     * 账号状态（0-禁用 1-启用）
     */
    private Integer status;

    /**
     * 逻辑删除（0-未删除 1-已删除）
     */
    private Integer isDeleted;


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
