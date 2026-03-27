package com.followup.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 随访计划表 实体
 *
 * @author system
 * @date 2026-03-23
 * @table follow_plan
 */
@Data
@TableName("follow_plan")
public class FollowPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 患者 ID
     */
    private Long patientId;

    /**
     * 医生 ID
     */
    private Long doctorId;

    /**
     * 计划类型（月度随访/季度随访/年度随访）
     */
    private String planType;

    /**
     * 周期（如：30 天、90 天、365 天）
     */
    private String cycle;

    /**
     * 下次随访时间
     */
    private LocalDate nextTime;

    /**
     * 状态（待执行/执行中/已完成）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

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
