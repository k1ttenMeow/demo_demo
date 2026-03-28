package com.followup.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followTime;

    /**
     * 患者姓名（关联查询字段，非数据库字段）
     */
    @TableField(exist = false)
    private String patientName;

    /**
     * 医生姓名（关联查询字段，非数据库字段）
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
