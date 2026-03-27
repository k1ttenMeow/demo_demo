package com.followup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.FollowAppoint;
import com.followup.entity.Patient;
import com.followup.entity.Doctor;
import com.followup.mapper.FollowAppointMapper;
import com.followup.service.FollowAppointService;
import com.followup.mapper.PatientMapper;
import com.followup.mapper.DoctorMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowAppointServiceImpl extends ServiceImpl<FollowAppointMapper, FollowAppoint> implements FollowAppointService {

    @Resource
    private FollowAppointMapper followAppointMapper;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Override
    public Page<FollowAppoint> getAppointList(Integer page, Integer size, String patientName, String doctorName, String status) {
        Page<FollowAppoint> followPage = new Page<>(page, size);

        LambdaQueryWrapper<FollowAppoint> wrapper = new LambdaQueryWrapper<>();

        // 动态添加查询条件
        if (StringUtils.hasText(patientName)) {
            // 先查询患者 ID
            List<Patient> patients = patientMapper.selectList(
                    new LambdaQueryWrapper<Patient>()
                            .like(StringUtils.hasText(patientName), Patient::getEmergencyContact, patientName)
            );
            List<Long> patientIds = patients.stream()
                    .map(Patient::getId)
                    .collect(Collectors.toList());

            if (!patientIds.isEmpty()) {
                wrapper.in(FollowAppoint::getPatientId, patientIds);
            } else {
                wrapper.eq(FollowAppoint::getPatientId, -1); // 无匹配
            }
        }

        if (StringUtils.hasText(doctorName)) {
            // 先查询医生 ID
            List<Doctor> doctors = doctorMapper.selectList(
                    new LambdaQueryWrapper<Doctor>()
                            .and(StringUtils.hasText(doctorName), w ->
                                    w.like(Doctor::getDepartment, doctorName)
                                            .or()
                                            .like(Doctor::getSkill, doctorName)
                            )
            );
            List<Long> doctorIds = doctors.stream()
                    .map(Doctor::getId)
                    .collect(Collectors.toList());

            if (!doctorIds.isEmpty()) {
                wrapper.in(FollowAppoint::getDoctorId, doctorIds);
            } else {
                wrapper.eq(FollowAppoint::getDoctorId, -1); // 无匹配
            }
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(FollowAppoint::getStatus, status);
        }

        // 按预约时间降序
        wrapper.orderByDesc(FollowAppoint::getAppointTime);

        return followAppointMapper.selectPage(followPage, wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean createAppoint(FollowAppoint appoint) {
        // 设置默认状态
        if (!StringUtils.hasText(appoint.getStatus())) {
            appoint.setStatus("待确认");
        }

        int result = followAppointMapper.insert(appoint);
        return result > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateAppoint(FollowAppoint appoint) {
        int result = followAppointMapper.updateById(appoint);
        return result > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteAppoint(Long id) {
        int result = followAppointMapper.deleteById(id);
        return result > 0;
    }
}
