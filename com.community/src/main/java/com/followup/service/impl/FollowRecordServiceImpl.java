package com.followup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.FollowRecord;
import com.followup.entity.Patient;
import com.followup.entity.Doctor;
import com.followup.mapper.FollowRecordMapper;
import com.followup.service.FollowRecordService;
import com.followup.mapper.PatientMapper;
import com.followup.mapper.DoctorMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowRecordServiceImpl extends ServiceImpl<FollowRecordMapper, FollowRecord> implements FollowRecordService {

    @Resource
    private FollowRecordMapper followRecordMapper;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Override
    public Page<FollowRecord> getRecordList(Integer page, Integer size, String patientName, String doctorName, String startDate, String endDate) {
        Page<FollowRecord> followPage = new Page<>(page, size);

        LambdaQueryWrapper<FollowRecord> wrapper = new LambdaQueryWrapper<>();

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
                wrapper.in(FollowRecord::getPatientId, patientIds);
            } else {
                wrapper.eq(FollowRecord::getPatientId, -1); // 无匹配
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
                wrapper.in(FollowRecord::getDoctorId, doctorIds);
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

        return followRecordMapper.selectPage(followPage, wrapper);
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
