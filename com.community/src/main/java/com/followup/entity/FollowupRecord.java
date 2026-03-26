package com.followup.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 随访记录表
 *
 * @author medical
 * @date 2026-03-26
 */
@Data
@TableName("followup_record")
public class FollowupRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 随访记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 患者ID（关联sys_user表）
     */
    @TableField("patient_id")
    private Long patientId;

    /**
     * 医生ID（关联sys_user表）
     */
    @TableField("doctor_id")
    private Long doctorId;

    /**
     * 随访内容
     */
    private String content;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}