package com.followup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.Doctor;
import com.followup.entity.FollowRecord;
import com.followup.entity.Patient;
import com.followup.entity.SysUser;
import com.followup.mapper.DoctorMapper;
import com.followup.mapper.FollowRecordMapper;
import com.followup.mapper.PatientMapper;
import com.followup.mapper.SysUserMapper;
import com.followup.service.DoctorService;
import com.followup.vo.DoctorDashboardVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private FollowRecordMapper followRecordMapper;

    @Resource
    private PatientMapper patientMapper;

    @Override
    public DoctorDashboardVO getDashboardInfo(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            return null;
        }

        DoctorDashboardVO vo = new DoctorDashboardVO();
        vo.setDoctorId(doctor.getId());
        vo.setUserId(doctor.getUserId());
        vo.setDepartment(doctor.getDepartment());
        vo.setSkill(doctor.getSkill());
        vo.setCommunity(doctor.getCommunity());
        vo.setTitle(doctor.getTitle());
        vo.setGender(doctor.getGender());
        vo.setIsOnline(doctor.getIsOnline());

        // 查询用户信息（姓名、手机号、状态）
        if (doctor.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(doctor.getUserId());
            if (user != null) {
                vo.setRealName(user.getRealName());
                vo.setPhone(user.getPhone());
                vo.setStatus(user.getStatus());
            }
        }

        return vo;
    }

    @Override
    public Page<Doctor> getDoctorList(Integer page, Integer size, String realName, String department, String community) {
        Page<Doctor> doctorPage = new Page<>(page, size);

        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();

        // 动态添加查询条件
        if (StringUtils.hasText(realName)) {
            wrapper.like(Doctor::getRealName, realName);
        }
        if (StringUtils.hasText(department)) {
            wrapper.eq(Doctor::getDepartment, department);
        }
        if (StringUtils.hasText(community)) {
            wrapper.eq(Doctor::getCommunity, community);
        }

        // 按创建时间降序
        wrapper.orderByDesc(Doctor::getCreateTime);

        return doctorMapper.selectPage(doctorPage, wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean createDoctor(Doctor doctor) {
        return this.save(doctor);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateDoctor(Doctor doctor) {
        return this.updateById(doctor);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteDoctor(Long id) {
        return this.removeById(id);
    }

    @Override
    public Map<String, Object> getStats(Long doctorId) {
        Map<String, Object> stats = new HashMap<>();

        // 查询负责的患者数量
        Long patientCount = patientMapper.selectCount(
                new LambdaQueryWrapper<Patient>()
                        .eq(Patient::getDoctorId, doctorId)
        );

        // 查询随访记录数量（使用 LambdaQueryWrapper）
        Long followCount = followRecordMapper.selectCount(
                new LambdaQueryWrapper<FollowRecord>()
                        .eq(FollowRecord::getDoctorId, doctorId)
        );

        stats.put("patientCount", patientCount != null ? patientCount : 0);
        stats.put("followCount", followCount != null ? followCount : 0);

        return stats;
    }

    @Override
    public Page<Map<String, Object>> getPatientList(Long doctorId, Integer page, Integer size) {
        Page<Map<String, Object>> resultPage = new Page<>(page, size);

        // 查询该医生负责的患者列表
        Page<Patient> patientPage = patientMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Patient>()
                        .eq(Patient::getDoctorId, doctorId)
                        .orderByDesc(Patient::getCreateTime)
        );

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Patient patient : patientPage.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", patient.getId());
            map.put("patientId", patient.getId());
            map.put("userId", patient.getUserId());
            map.put("chronicType", patient.getChronicType());
            map.put("age", patient.getAge());
            map.put("address", patient.getAddress());
            map.put("gender", patient.getGender());

            // 查询用户信息
            if (patient.getUserId() != null) {
                SysUser user = sysUserMapper.selectById(patient.getUserId());
                if (user != null) {
                    map.put("realName", user.getRealName());
                    map.put("phone", user.getPhone());
                    map.put("username", user.getUsername());
                }
            }

            resultList.add(map);
        }

        resultPage.setRecords(resultList);
        resultPage.setTotal(patientPage.getTotal());
        resultPage.setSize(patientPage.getSize());
        resultPage.setCurrent(patientPage.getCurrent());

        return resultPage;
    }
}
