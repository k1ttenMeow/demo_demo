package com.followup.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 随访预约表 实体
 *
 * @author system
 * @date 2026-03-23
 * @table follow_appoint
 */
@Data
@TableName("follow_appoint")
public class FollowAppoint implements Serializable {

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
     * 预约时间
     */
    private LocalDate appointTime;

    /**
     * 状态（待确认/已确认/已完成/已取消）
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
