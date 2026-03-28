package com.followup.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 医生仪表板视图对象
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
     * 职称
     */
    private String title;

    /**
     * 是否在线（0-离线 1-在线）
     */
    private Integer isOnline;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态
     */
    private Integer status;
}
