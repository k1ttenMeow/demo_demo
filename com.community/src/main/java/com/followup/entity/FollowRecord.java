package com.followup.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 随访记录表 实体
 *
 * @author system
 * @date 2026-03-23
 * @table follow_record
 */
@Data
@TableName("follow_record")
public class FollowRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 随访计划 ID（可为空）
     */
    private Long planId;

    /**
     * 患者 ID
     */
    private Long patientId;

    /**
     * 医生 ID
     */
    private Long doctorId;

    /**
     * 血压
     */
    private String bloodPressure;

    /**
     * 血糖
     */
    private String bloodSugar;

    /**
     * 用药情况
     */
    private String drug;

    /**
     * 症状
     */
    private String symptom;

    /**
     * 备注
     */
    private String remark;

    /**
     * 随访时间
     */
    private LocalDateTime followTime;

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
