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
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID（关联 user 表）
     */
    private Long userId;

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
     * 创建时间（非数据库字段）
     */
    @TableField(exist = false)
    private LocalDateTime createTime;

    /**
     * 更新时间（非数据库字段）
     */
    @TableField(exist = false)
    private LocalDateTime updateTime;
}
