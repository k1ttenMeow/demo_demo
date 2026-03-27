package com.followup.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.followup.entity.Patient;
import com.followup.vo.PatientDashboardVO;

/**
 * 患者表 业务接口
 *
 * @author system
 * @date 2026-03-23
 */
public interface PatientService extends IService<Patient> {

    /**
     * 分页查询患者列表
     * @param page 页码
     * @param size 每页大小
     * @param realName 患者姓名
     * @param phone 手机号
     * @param chronicType 慢病类型
     * @param doctorName 责任医生
     * @return 分页结果
     */
    Page<Patient> getPatientList(Integer page, Integer size, String realName, String phone, String chronicType, String doctorName);

    /**
     * 获取患者仪表板信息
     * @param patientId 患者 ID
     * @return 患者仪表板 VO
     */
    PatientDashboardVO getDashboardInfo(Long patientId);

    /**
     * 创建患者信息
     * @param patient 患者信息
     * @return 是否成功
     */
    boolean createPatient(Patient patient);

    /**
     * 更新患者信息
     * @param patient 患者信息
     * @return 是否成功
     */
    boolean updatePatient(Patient patient);

    /**
     * 删除患者信息
     * @param id 患者 ID
     * @return 是否成功
     */
    boolean deletePatient(Long id);
}
