package com.followup.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医生表 实体
 *
 * @author system
 * @date 2026-03-23
 */
@Data
@TableName("doctor")
public class Doctor implements Serializable {

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
     * 性别（0-未知 1-男 2-女）
     */
    private Integer gender;

    /**
     * 所属科室
     */
    private String department;

    /**
     * 擅长技能
     */
    private String skill;

    /**
     * 所属社区
     */
    private String community;

    /**
     * 职称（如：主治医师、副主任医师、主任医师）
     */
    private String title;

    /**
     * 是否在线（0-离线 1-在线）
     */
    private Integer isOnline;

    /**
     * 医生姓名（关联查询字段，非数据库字段）
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
