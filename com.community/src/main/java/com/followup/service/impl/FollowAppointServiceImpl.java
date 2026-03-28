package com.followup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.FollowAppoint;
import com.followup.entity.Patient;
import com.followup.entity.Doctor;
import com.followup.entity.SysUser;
import com.followup.mapper.FollowAppointMapper;
import com.followup.mapper.PatientMapper;
import com.followup.mapper.DoctorMapper;
import com.followup.mapper.SysUserMapper;
import com.followup.service.FollowAppointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FollowAppointServiceImpl extends ServiceImpl<FollowAppointMapper, FollowAppoint> implements FollowAppointService {

    @Resource
    private FollowAppointMapper followAppointMapper;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public Page<FollowAppoint> getAppointList(Integer page, Integer size, String patientName, String doctorName, String status) {
        Page<FollowAppoint> followPage = new Page<>(page, size);

        LambdaQueryWrapper<FollowAppoint> wrapper = new LambdaQueryWrapper<>();

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
                wrapper.in(FollowAppoint::getPatientId, userIds);
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

        // 执行查询
        Page<FollowAppoint> result = followAppointMapper.selectPage(followPage, wrapper);

        // 填充患者和医生姓名
        List<FollowAppoint> appoints = result.getRecords();
        if (!appoints.isEmpty()) {
            System.out.println("=== 开始填充随访预约数据 ===");

            // 批量获取所有患者 ID（这些 ID 直接对应 user.id）
            List<Long> patientIds = appoints.stream()
                    .map(FollowAppoint::getPatientId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            System.out.println("患者 ID 列表：" + patientIds);

            // 批量获取所有医生 ID
            List<Long> doctorIds = appoints.stream()
                    .map(FollowAppoint::getDoctorId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            System.out.println("医生 ID 列表：" + doctorIds);

            // 查询患者信息（follow_appoint.patient_id 直接关联 user.id）
            Map<Long, String> patientNameMap = new HashMap<>();
            if (!patientIds.isEmpty()) {
                System.out.println("开始查询患者信息（直接从 user 表）...");
                // 直接从 user 表查询
                List<SysUser> patients = sysUserMapper.selectBatchIds(patientIds);
                System.out.println("从 user 表查询到 " + patients.size() + " 个患者");
                for (SysUser user : patients) {
                    System.out.println("  用户 ID: " + user.getId() + ", 姓名：" + user.getRealName());
                    patientNameMap.put(user.getId(), user.getRealName());
                }
            } else {
                System.out.println("没有患者 ID 需要查询");
            }

            // 查询医生信息（通过 doctor 表关联 user 表获取姓名）
            Map<Long, String> doctorNameMap = new HashMap<>();
            if (!doctorIds.isEmpty()) {
                System.out.println("开始查询医生信息...");
                for (Long doctorId : doctorIds) {
                    Doctor doctor = doctorMapper.selectById(doctorId);
                    System.out.println("医生 ID " + doctorId + " 查询结果：" + (doctor != null ? "找到" : "未找到"));
                    if (doctor != null && doctor.getUserId() != null) {
                        SysUser user = sysUserMapper.selectById(doctor.getUserId());
                        if (user != null) {
                            System.out.println("  医生姓名：" + user.getRealName());
                            doctorNameMap.put(doctorId, user.getRealName());
                        }
                    }
                }
            }

            // 填充每个预约的关联信息
            for (FollowAppoint appoint : appoints) {
                String pName = patientNameMap.get(appoint.getPatientId());
                String dName = doctorNameMap.get(appoint.getDoctorId());
                System.out.println("预约 ID " + appoint.getId() + " - 患者 ID: " + appoint.getPatientId() +
                        ", 患者姓名：" + (pName != null ? pName : "null") +
                        ", 医生 ID: " + appoint.getDoctorId() +
                        ", 医生姓名：" + (dName != null ? dName : "null"));

                if (pName != null) {
                    appoint.setPatientName(pName);
                }

                if (dName != null) {
                    appoint.setDoctorName(dName);
                }
            }

            System.out.println("=== 填充完成 ===");
        }

        return result;
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
