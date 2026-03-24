package com.followup.util;

import com.followup.constant.RoleConstant;

/**
 * 角色校验工具类（简单版，后续可扩展为拦截器/注解）
 */
public class RoleCheckUtil {

    /**
     * 校验是否为管理员
     */
    public static boolean isAdmin(Integer userType) {
        return RoleConstant.ADMIN.equals(userType);
    }

    /**
     * 校验是否为医生
     */
    public static boolean isDoctor(Integer userType) {
        return RoleConstant.DOCTOR.equals(userType);
    }

    /**
     * 校验是否为患者
     */
    public static boolean isPatient(Integer userType) {
        return RoleConstant.PATIENT.equals(userType);
    }

    /**
     * 校验是否有管理员或医生权限（可访问患者管理）
     */
    public static boolean hasMedicalPermission(Integer userType) {
        return isAdmin(userType) || isDoctor(userType);
    }
}