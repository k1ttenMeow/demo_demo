package com.followup.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.followup.entity.Doctor;
import com.followup.vo.DoctorDashboardVO;

import java.util.Map;

/**
 * 医生表 业务接口
 *
 * @author system
 * @date 2026-03-23
 */
public interface DoctorService extends IService<Doctor> {

    /**
     * 分页查询医生列表
     * @param page 页码
     * @param size 每页大小
     * @param realName 医生姓名
     * @param department 科室
     * @param community 社区
     * @return 分页结果
     */
    Page<Doctor> getDoctorList(Integer page, Integer size, String realName, String department, String community);

    /**
     * 获取医生仪表板信息
     * @param doctorId 医生 ID
     * @return 医生仪表板 VO
     */
    DoctorDashboardVO getDashboardInfo(Long doctorId);

    /**
     * 创建医生信息
     * @param doctor 医生信息
     * @return 是否成功
     */
    boolean createDoctor(Doctor doctor);

    /**
     * 更新医生信息
     * @param doctor 医生信息
     * @return 是否成功
     */
    boolean updateDoctor(Doctor doctor);

    /**
     * 删除医生信息
     * @param id 医生 ID
     * @return 是否成功
     */
    boolean deleteDoctor(Long id);

    /**
     * 获取统计数据
     * @param doctorId 医生 ID
     * @return 统计数据
     */
    Map<String, Object> getStats(Long doctorId);

    /**
     * 获取患者列表
     * @param doctorId 医生 ID
     * @param page 页码
     * @param size 每页大小
     * @return 患者列表分页结果
     */
    Page<Map<String, Object>> getPatientList(Long doctorId, Integer page, Integer size);

    /**
     * 获取患者列表（支持搜索）
     * @param doctorId 医生 ID
     * @param page 页码
     * @param size 每页大小
     * @param realName 患者姓名
     * @param chronicType 慢病类型
     * @return 患者列表分页结果
     */
    Page<Map<String, Object>> getPatientList(Long doctorId, Integer page, Integer size, String realName, String chronicType);

    /**
     * 更新医生在线状态
     * @param doctorId 医生 ID
     * @param isOnline 在线状态（0-离线 1-在线）
     * @return 是否成功
     */
    boolean updateOnlineStatus(Long doctorId, Integer isOnline);

    /**
     * 获取医生的预约列表
     * @param doctorId 医生 ID
     * @param page 页码
     * @param size 每页大小
     * @param patientName 患者姓名
     * @param status 预约状态
     * @param appointTime 预约时间
     * @return 预约列表分页结果
     */
    Page<Map<String, Object>> getMyAppointments(
            Long doctorId,
            Integer page,
            Integer size,
            String patientName,
            String status,
            String appointTime
    );

    /**
     * 获取医生统计数据
     * @param doctorId 医生 ID
     * @return 统计数据
     */
    Map<String, Object> getDoctorStats(Long doctorId);
}
