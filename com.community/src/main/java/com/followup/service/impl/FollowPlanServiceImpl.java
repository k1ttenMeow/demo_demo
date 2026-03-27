package com.followup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.FollowPlan;
import com.followup.entity.Patient;
import com.followup.entity.Doctor;
import com.followup.mapper.FollowPlanMapper;
import com.followup.service.FollowPlanService;
import com.followup.mapper.PatientMapper;
import com.followup.mapper.DoctorMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowPlanServiceImpl extends ServiceImpl<FollowPlanMapper, FollowPlan> implements FollowPlanService {

    @Resource
    private FollowPlanMapper followPlanMapper;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Override
    public Page<FollowPlan> getPlanList(Integer page, Integer size, String patientName, String doctorName, String planType, String status) {
        Page<FollowPlan> followPage = new Page<>(page, size);

        LambdaQueryWrapper<FollowPlan> wrapper = new LambdaQueryWrapper<>();

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
                wrapper.in(FollowPlan::getPatientId, patientIds);
            } else {
                wrapper.eq(FollowPlan::getPatientId, -1); // 无匹配
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
                wrapper.in(FollowPlan::getDoctorId, doctorIds);
            } else {
                wrapper.eq(FollowPlan::getDoctorId, -1); // 无匹配
            }
        }

        if (StringUtils.hasText(planType)) {
            wrapper.eq(FollowPlan::getPlanType, planType);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(FollowPlan::getStatus, status);
        }

        // 按下次随访时间升序
        wrapper.orderByAsc(FollowPlan::getNextTime);

        return followPlanMapper.selectPage(followPage, wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean createPlan(FollowPlan plan) {
        // 设置默认状态
        if (!StringUtils.hasText(plan.getStatus())) {
            plan.setStatus("待执行");
        }

        int result = followPlanMapper.insert(plan);
        return result > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updatePlan(FollowPlan plan) {
        int result = followPlanMapper.updateById(plan);
        return result > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deletePlan(Long id) {
        int result = followPlanMapper.deleteById(id);
        return result > 0;
    }
}
