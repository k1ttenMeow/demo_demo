package com.followup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.FollowPlan;
import com.followup.entity.Patient;
import com.followup.entity.Doctor;
import com.followup.entity.SysUser;
import com.followup.mapper.FollowPlanMapper;
import com.followup.service.FollowPlanService;
import com.followup.mapper.PatientMapper;
import com.followup.mapper.DoctorMapper;
import com.followup.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FollowPlanServiceImpl extends ServiceImpl<FollowPlanMapper, FollowPlan> implements FollowPlanService {

    @Resource
    private FollowPlanMapper followPlanMapper;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public Page<FollowPlan> getPlanList(Integer page, Integer size, String patientName, String doctorName, String planType, String status) {
        Page<FollowPlan> followPage = new Page<>(page, size);

        LambdaQueryWrapper<FollowPlan> wrapper = new LambdaQueryWrapper<>();

        // 动态添加查询条件
        if (StringUtils.hasText(patientName)) {
            // 先查询患者 ID（从 user 表）
            List<SysUser> users = sysUserMapper.selectList(
                    new LambdaQueryWrapper<SysUser>()
                            .like(StringUtils.hasText(patientName), SysUser::getRealName, patientName)
            );
            List<Long> userIds = users.stream()
                    .map(SysUser::getId)
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                wrapper.in(FollowPlan::getPatientId, userIds);
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

        // 执行查询
        Page<FollowPlan> result = followPlanMapper.selectPage(followPage, wrapper);

        // 填充患者和医生姓名
        List<FollowPlan> plans = result.getRecords();
        if (!plans.isEmpty()) {
            // 批量获取所有患者 ID（这些 ID 直接对应 user.id）
            List<Long> patientIds = plans.stream()
                    .map(FollowPlan::getPatientId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量获取所有医生 ID
            List<Long> doctorIds = plans.stream()
                    .map(FollowPlan::getDoctorId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            // 查询患者信息（follow_plan.patient_id 直接关联 user.id）
            Map<Long, String> patientNameMap = new HashMap<>();
            if (!patientIds.isEmpty()) {
                List<SysUser> patients = sysUserMapper.selectBatchIds(patientIds);
                for (SysUser user : patients) {
                    patientNameMap.put(user.getId(), user.getRealName());
                }
            }

            // 查询医生信息（通过 doctor 表关联 user 表获取姓名）
            Map<Long, String> doctorNameMap = new HashMap<>();
            if (!doctorIds.isEmpty()) {
                for (Long doctorId : doctorIds) {
                    Doctor doctor = doctorMapper.selectById(doctorId);
                    if (doctor != null && doctor.getUserId() != null) {
                        SysUser user = sysUserMapper.selectById(doctor.getUserId());
                        if (user != null) {
                            doctorNameMap.put(doctorId, user.getRealName());
                        }
                    }
                }
            }

            // 填充每个计划的关联信息
            for (FollowPlan plan : plans) {
                String pName = patientNameMap.get(plan.getPatientId());
                if (pName != null) {
                    plan.setPatientName(pName);
                }

                String dName = doctorNameMap.get(plan.getDoctorId());
                if (dName != null) {
                    plan.setDoctorName(dName);
                }
            }
        }

        return result;
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
