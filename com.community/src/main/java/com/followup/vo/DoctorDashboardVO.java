package com.followup.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 医生仪表板视图对象
 *
 * @author system
 * @date 2026-03-27
 */
@Data
public class DoctorDashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 医生 ID
     */
    private Long doctorId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 科室
     */
    private String department;

    /**
     * 技能
     */
    private String skill;

    /**
     * 所属社区
     */
    private String community;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态
     */
    private Integer status;
}
