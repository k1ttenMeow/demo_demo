package com.followup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.FollowRecord;
import com.followup.entity.Patient;
import com.followup.entity.Doctor;
import com.followup.entity.SysUser;
import com.followup.mapper.FollowRecordMapper;
import com.followup.service.FollowRecordService;
import com.followup.mapper.PatientMapper;
import com.followup.mapper.DoctorMapper;
import com.followup.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FollowRecordServiceImpl extends ServiceImpl<FollowRecordMapper, FollowRecord> implements FollowRecordService {

    @Resource
    private FollowRecordMapper followRecordMapper;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public Page<FollowRecord> getRecordList(Integer page, Integer size, String patientName, String doctorName, String startDate, String endDate) {
        Page<FollowRecord> followPage = new Page<>(page, size);

        LambdaQueryWrapper<FollowRecord> wrapper = new LambdaQueryWrapper<>();

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
                wrapper.in(FollowRecord::getPatientId, userIds);
            } else {
                wrapper.eq(FollowRecord::getPatientId, -1); // 无匹配
            }
        }

        if (StringUtils.hasText(doctorName)) {
            // 先查询医生 ID（从 user 表）
            List<SysUser> users = sysUserMapper.selectList(
                    new LambdaQueryWrapper<SysUser>()
                            .like(StringUtils.hasText(doctorName), SysUser::getRealName, doctorName)
            );
            List<Long> userIds = users.stream()
                    .map(SysUser::getId)
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                wrapper.in(FollowRecord::getDoctorId, userIds);
            } else {
                wrapper.eq(FollowRecord::getDoctorId, -1); // 无匹配
            }
        }

        // 日期范围查询
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
            wrapper.ge(FollowRecord::getFollowTime, startDateTime);
        }

        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
            wrapper.lt(FollowRecord::getFollowTime, endDateTime);
        }

        // 按随访时间降序
        wrapper.orderByDesc(FollowRecord::getFollowTime);

        // 执行查询
        Page<FollowRecord> result = followRecordMapper.selectPage(followPage, wrapper);

        // 填充患者和医生姓名
        List<FollowRecord> records = result.getRecords();
        if (!records.isEmpty()) {
            System.out.println("=== 开始填充随访记录数据 ===");

            // 批量获取所有患者 ID（这些 ID 直接对应 user.id）
            List<Long> patientIds = records.stream()
                    .map(FollowRecord::getPatientId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            System.out.println("患者 ID 列表：" + patientIds);

            // 批量获取所有医生 ID（这些 ID 也直接对应 user.id）
            List<Long> doctorIds = records.stream()
                    .map(FollowRecord::getDoctorId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            System.out.println("医生 ID 列表：" + doctorIds);

            // 查询患者信息（follow_record.patient_id 直接关联 user.id）
            Map<Long, String> patientNameMap = new HashMap<>();
            if (!patientIds.isEmpty()) {
                List<SysUser> patients = sysUserMapper.selectBatchIds(patientIds);
                System.out.println("查询到患者数量：" + patients.size());
                for (SysUser user : patients) {
                    System.out.println("  患者 ID: " + user.getId() + ", 姓名：" + user.getRealName());
                    patientNameMap.put(user.getId(), user.getRealName());
                }
            }

            // 查询医生信息（follow_record.doctor_id 是 doctor 表的 id，需要关联查询）
            Map<Long, String> doctorNameMap = new HashMap<>();
            if (!doctorIds.isEmpty()) {
                System.out.println("开始查询医生信息（通过 doctor 表关联）...");
                for (Long doctorId : doctorIds) {
                    Doctor doctor = doctorMapper.selectById(doctorId);
                    System.out.println("  查询 doctor_id=" + doctorId + ": " + (doctor != null ? "找到" : "未找到"));
                    if (doctor != null && doctor.getUserId() != null) {
                        SysUser user = sysUserMapper.selectById(doctor.getUserId());
                        System.out.println("    user_id=" + doctor.getUserId() + ", 姓名=" + user.getRealName());
                        if (user != null) {
                            doctorNameMap.put(doctorId, user.getRealName());
                        }
                    } else {
                        System.out.println("    未找到对应的 user_id");
                    }
                }
            }

            // 填充每个记录的关联信息
            for (FollowRecord record : records) {
                String pName = patientNameMap.get(record.getPatientId());
                String dName = doctorNameMap.get(record.getDoctorId());
                System.out.println("记录 ID " + record.getId() +
                        " - 患者 ID: " + record.getPatientId() +
                        ", 患者姓名：" + (pName != null ? pName : "null") +
                        " - 医生 ID: " + record.getDoctorId() +
                        ", 医生姓名：" + (dName != null ? dName : "null"));

                if (pName != null) {
                    record.setPatientName(pName);
                }

                if (dName != null) {
                    record.setDoctorName(dName);
                }
            }

            System.out.println("=== 填充完成 ===");
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean createRecord(FollowRecord record) {
        // 设置默认随访时间为当前时间
        if (record.getFollowTime() == null) {
            record.setFollowTime(LocalDateTime.now());
        }

        int result = followRecordMapper.insert(record);
        return result > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateRecord(FollowRecord record) {
        int result = followRecordMapper.updateById(record);
        return result > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteRecord(Long id) {
        int result = followRecordMapper.deleteById(id);
        return result > 0;
    }
}
