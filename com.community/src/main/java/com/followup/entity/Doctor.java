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
 * @table doctor
 */
@Data
@TableName("doctor")
public class Doctor implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 所属科室
     */
    private String department;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 职称
     */
    private String title;

    /**
     * 用户ID（关联user表）
     */
    private Long userId;

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

    /**
     * 逻辑删除 0-未删除 1-已删除
     */
    @TableLogic
    @TableField(value = "is_deleted")
    private Integer isDeleted;
}