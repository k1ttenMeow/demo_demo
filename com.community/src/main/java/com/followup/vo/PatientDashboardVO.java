package com.followup.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 患者仪表板视图对象
 *
 * @author system
 * @date 2026-03-27
 */
@Data
public class PatientDashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 患者 ID
     */
    private Long patientId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 慢病类型
     */
    private String chronicType;

    /**
     * 责任医生姓名
     */
    private String doctorName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 地址
     */
    private String address;
}
