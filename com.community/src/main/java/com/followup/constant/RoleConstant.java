package com.followup.constant;

/**
 * 角色常量类（统一管理所有角色类型）
 */
public class RoleConstant {
    // 管理员：拥有所有权限
    public static final Integer ADMIN = 1;
    // 医生：可管理患者、查看随访记录
    public static final Integer DOCTOR = 2;
    // 患者：仅可查看个人信息、提交随访
    public static final Integer PATIENT = 3;
}