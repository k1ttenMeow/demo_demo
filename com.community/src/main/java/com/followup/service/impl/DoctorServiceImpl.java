package com.followup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.Doctor;
import com.followup.entity.SysUser;
import com.followup.entity.Patient;
import com.followup.entity.FollowAppoint;
import com.followup.entity.FollowPlan;
import com.followup.entity.FollowRecord;
import com.followup.mapper.DoctorMapper;
import com.followup.mapper.SysUserMapper;
import com.followup.mapper.PatientMapper;
import com.followup.mapper.FollowAppointMapper;
import com.followup.mapper.FollowPlanMapper;
import com.followup.mapper.FollowRecordMapper;
import com.followup.service.DoctorService;
import com.followup.vo.DoctorDashboardVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private FollowAppointMapper followAppointMapper;

    @Resource
    private FollowPlanMapper followPlanMapper;

    @Resource
    private FollowRecordMapper followRecordMapper;

    @Override
    public Page<Doctor> getDoctorList(Integer page, Integer size, String realName, String department, String community) {
        Page<Doctor> doctorPage = new Page<>(page, size);

        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();

        // 动态添加查询条件
        if (StringUtils.hasText(realName)) {
            // 先查询用户 ID
            List<SysUser> users = sysUserMapper.selectList(
                    new LambdaQueryWrapper<SysUser>()
                            .like(SysUser::getRealName, realName)
                            .eq(SysUser::getRole, 2) // 只查询医生角色
            );
            List<Long> userIds = users.stream()
                    .map(SysUser::getId)
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                wrapper.in(Doctor::getUserId, userIds);
            } else {
                wrapper.eq(Doctor::getUserId, -1); // 无匹配
            }
        }

        if (StringUtils.hasText(department)) {
            wrapper.like(Doctor::getDepartment, department);
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
        System.out.println("=== 开始创建医生 ===");
        System.out.println("username: " + doctor.getUsername());
        System.out.println("realName: " + doctor.getRealName());
        System.out.println("phone: " + doctor.getPhone());

        // 1. 创建用户信息（user 表）
        SysUser user = new SysUser();
        user.setUsername(doctor.getUsername());
        user.setPassword("{bcrypt}" + new BCryptPasswordEncoder().encode(doctor.getPassword()));
        user.setRealName(doctor.getRealName());
        user.setPhone(doctor.getPhone());
        user.setRole(2); // 医生角色
        user.setStatus(doctor.getStatus() != null ? doctor.getStatus() : 1);

        int userResult = sysUserMapper.insert(user);
        System.out.println("插入 user 表结果：" + userResult + ", userId: " + user.getId());

        if (userResult > 0 && user.getId() != null) {
            // 2. 创建医生信息（doctor 表）
            doctor.setUserId(user.getId());

            // 根据手机号自动设置性别（简单规则：奇数男，偶数女）
            if (doctor.getGender() == null && doctor.getPhone() != null) {
                String lastDigit = doctor.getPhone().substring(doctor.getPhone().length() - 1);
                doctor.setGender(Integer.parseInt(lastDigit) % 2 == 1 ? 1 : 2);
            }

            int doctorResult = doctorMapper.insert(doctor);
            System.out.println("插入 doctor 表结果：" + doctorResult);
            return doctorResult > 0;
        }

        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateDoctor(Doctor doctor) {
        System.out.println("=== 开始更新医生 ===");
        System.out.println("userId: " + doctor.getUserId());

        // 1. 更新用户信息（user 表）
        if (doctor.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(doctor.getUserId());
            if (user != null) {
                user.setRealName(doctor.getRealName());
                user.setPhone(doctor.getPhone());
                user.setStatus(doctor.getStatus() != null ? doctor.getStatus() : 1);

                // 如果填写了密码则更新密码
                if (StringUtils.hasText(doctor.getPassword())) {
                    user.setPassword("{bcrypt}" + new BCryptPasswordEncoder().encode(doctor.getPassword()));
                }

                int userResult = sysUserMapper.updateById(user);
                System.out.println("更新 user 表结果：" + userResult);
            }
        }

        // 2. 更新医生信息（doctor 表）
        // 根据手机号自动设置性别
        if (doctor.getGender() == null && doctor.getPhone() != null) {
            String lastDigit = doctor.getPhone().substring(doctor.getPhone().length() - 1);
            doctor.setGender(Integer.parseInt(lastDigit) % 2 == 1 ? 1 : 2);
        }

        int doctorResult = doctorMapper.updateById(doctor);
        System.out.println("更新 doctor 表结果：" + doctorResult);

        return doctorResult > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteDoctor(Long id) {
        int result = doctorMapper.deleteById(id);
        return result > 0;
    }

    @Override
    public DoctorDashboardVO getDashboardInfo(Long doctorId) {
        System.out.println("=== 获取医生仪表板信息 ===");
        System.out.println("传入的 doctorId: " + doctorId);

        // ✅ 直接假设传入的 doctorId 是 user.id，从 user 表查询
        SysUser user = sysUserMapper.selectById(doctorId);

        if (user == null || user.getRole() != 2) {
            System.out.println("用户不存在或不是医生角色");
            return null;
        }

        System.out.println("找到用户：" + user.getRealName() + ", user.id: " + user.getId());

        // 通过 user_id 查找对应的 doctor 记录
        List<Doctor> doctors = doctorMapper.selectList(
                new LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getUserId, doctorId)
        );

        if (doctors.isEmpty()) {
            System.out.println("未找到对应的医生档案");
            return null;
        }

        Doctor doctor = doctors.get(0);
        System.out.println("找到医生档案，doctor.id: " + doctor.getId());
        System.out.println("医生 user.id: " + doctor.getUserId());

        DoctorDashboardVO vo = new DoctorDashboardVO();
        vo.setDoctorId(doctor.getId());
        vo.setUserId(doctor.getUserId());
        vo.setDepartment(doctor.getDepartment());
        vo.setSkill(doctor.getSkill());
        vo.setCommunity(doctor.getCommunity());
        vo.setTitle(doctor.getTitle());
        vo.setGender(doctor.getGender());
        vo.setIsOnline(doctor.getIsOnline());

        // 设置性别文本
        if (doctor.getGender() == 1) {
            vo.setGenderText("男");
        } else if (doctor.getGender() == 2) {
            vo.setGenderText("女");
        } else {
            vo.setGenderText("未知");
        }

        // 设置在线状态文本
        if (doctor.getIsOnline() == 1) {
            vo.setIsOnlineText("在线");
        } else {
            vo.setIsOnlineText("离线");
        }

        // 查询用户信息（姓名、手机号、状态）
        if (doctor.getUserId() != null) {
            SysUser userInfo = sysUserMapper.selectById(doctor.getUserId());
            if (userInfo != null) {
                vo.setRealName(userInfo.getRealName());
                vo.setPhone(userInfo.getPhone());
                vo.setStatus(userInfo.getStatus());

                // 设置状态文本
                if (userInfo.getStatus() == 1) {
                    vo.setStatusText("正常");
                } else if (userInfo.getStatus() == 0) {
                    vo.setStatusText("禁用");
                } else {
                    vo.setStatusText("未知");
                }
            }
        }

        return vo;
    }

    @Override
    public Map<String, Object> getStats(Long doctorId) {
        Map<String, Object> stats = new HashMap<>();

        // 查询医生管理的患者数量
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Patient::getDoctorId, doctorId);
        Long patientCount = patientMapper.selectCount(wrapper);

        stats.put("patientCount", patientCount);

        return stats;
    }

    /**
     * ✅ 保留原有方法（不做任何修改）
     */
    @Override
    public Page<Map<String, Object>> getPatientList(Long doctorId, Integer page, Integer size) {
        // 调用重载方法，不传搜索参数
        return getPatientList(doctorId, page, size, null, null);
    }

    /**
     * ✅ 新增重载方法（支持搜索）- 正确逻辑：patient.doctor_id 对应 doctor.id
     */
    @Override
    public Page<Map<String, Object>> getPatientList(Long doctorId, Integer page, Integer size, String realName, String chronicType) {
        System.out.println("=== 开始查询医生患者列表 ===");
        System.out.println("doctorId (user.id): " + doctorId);

        Page<Patient> patientPage = new Page<>(page, size);

        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();

        // ✅ 先通过 user.id 找到 doctor.id，再查询 patient 表
        // 因为 patient.doctor_id 对应的是 doctor 表的主键 id
        List<Doctor> doctors = doctorMapper.selectList(
                new LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getUserId, doctorId)
        );

        if (doctors.isEmpty()) {
            System.out.println("未找到医生档案");
            Page<Map<String, Object>> emptyPage = new Page<>(page, size);
            emptyPage.setTotal(0);
            return emptyPage;
        }

        Doctor doctor = doctors.get(0);
        Long actualDoctorId = doctor.getId(); // 这是 doctor 表的主键 id

        System.out.println("医生档案：doctor.id=" + actualDoctorId + ", user.id=" + doctorId);

        // 使用 doctor.id 查询患者（patient.doctor_id 对应 doctor.id）
        wrapper.eq(Patient::getDoctorId, actualDoctorId);

        // 动态添加查询条件
        if (StringUtils.hasText(realName)) {
            // 先查询患者用户 ID（从 user 表）
            List<SysUser> users = sysUserMapper.selectList(
                    new LambdaQueryWrapper<SysUser>()
                            .like(StringUtils.hasText(realName), SysUser::getRealName, realName)
                            .eq(SysUser::getRole, 3) // 只查询患者角色
            );
            List<Long> userIds = users.stream()
                    .map(SysUser::getId)
                    .collect(Collectors.toList());

            System.out.println("患者姓名：" + realName + ", 找到的患者用户 IDs: " + userIds);

            if (!userIds.isEmpty()) {
                wrapper.in(Patient::getUserId, userIds);
            } else {
                wrapper.eq(Patient::getUserId, -1); // 无匹配
            }
        }

        if (StringUtils.hasText(chronicType)) {
            wrapper.eq(Patient::getChronicType, chronicType);
        }

        wrapper.orderByDesc(Patient::getCreateTime);

        Page<Patient> result = patientMapper.selectPage(patientPage, wrapper);

        // 转换为 Map 格式并填充用户信息
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Patient> patients = result.getRecords();

        if (!patients.isEmpty()) {
            // 批量获取所有用户 ID
            List<Long> userIds = patients.stream()
                    .map(Patient::getUserId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            // 查询用户信息
            Map<Long, SysUser> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
                for (SysUser user : users) {
                    userMap.put(user.getId(), user);
                }
            }

            // 构建返回结果
            for (Patient patient : patients) {
                Map<String, Object> patientMap = new HashMap<>();
                patientMap.put("id", patient.getId());
                patientMap.put("userId", patient.getUserId());
                patientMap.put("chronicType", patient.getChronicType());
                patientMap.put("age", patient.getAge());
                patientMap.put("address", patient.getAddress());
                patientMap.put("gender", patient.getGender());
                patientMap.put("emergencyContact", patient.getEmergencyContact());
                patientMap.put("emergencyPhone", patient.getEmergencyPhone());

                // 填充用户信息（患者姓名、电话等）
                SysUser user = userMap.get(patient.getUserId());
                if (user != null) {
                    patientMap.put("realName", user.getRealName());
                    patientMap.put("phone", user.getPhone());
                    patientMap.put("username", user.getUsername());
                    patientMap.put("status", user.getStatus());
                } else {
                    patientMap.put("realName", null);
                    patientMap.put("phone", null);
                    patientMap.put("username", null);
                    patientMap.put("status", null);
                }

                resultList.add(patientMap);

                System.out.println("✅ 患者：" + (user != null ? user.getRealName() : "未知") +
                        " | patient.doctor_id=" + patient.getDoctorId() +
                        " (对应 doctor.id)");
            }
        }

        // 创建新的 Page 对象
        Page<Map<String, Object>> resultPage = new Page<>(page, size);
        resultPage.setRecords(resultList);
        resultPage.setTotal(result.getTotal());

        System.out.println("查询到的患者数量：" + resultList.size());

        return resultPage;
    }

    @Override
    public boolean updateOnlineStatus(Long doctorId, Integer isOnline) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
        }

        doctor.setIsOnline(isOnline);
        int result = doctorMapper.updateById(doctor);
        return result > 0;
    }

    @Override
    public Page<Map<String, Object>> getMyAppointments(
            Long doctorId,
            Integer page,
            Integer size,
            String patientName,
            String status,
            String appointTime
    ) {
        System.out.println("=== 开始查询医生预约 ===");
        System.out.println("传入的 doctorId (应该是 user.id): " + doctorId);
        System.out.println("page: " + page + ", size: " + size);

        // ✅ 将 user.id 转换为 doctor.id
        Doctor doctorRecord = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getUserId, doctorId)
        );

        if (doctorRecord == null) {
            System.out.println("未找到医生档案记录");
            return new Page<>(page, size);
        }

        Long actualDoctorId = doctorRecord.getId();
        System.out.println("转换后的 doctor.id: " + actualDoctorId);

        // 直接查询预约列表
        Page<FollowAppoint> followPage = new Page<>(page, size);

        LambdaQueryWrapper<FollowAppoint> wrapper = new LambdaQueryWrapper<>();

        // ✅ 使用 doctor.id 进行查询
        wrapper.eq(FollowAppoint::getDoctorId, actualDoctorId);

        // 动态添加查询条件
        if (StringUtils.hasText(patientName)) {
            // 先查询患者用户 ID（从 user 表）
            List<SysUser> users = sysUserMapper.selectList(
                    new LambdaQueryWrapper<SysUser>()
                            .like(StringUtils.hasText(patientName), SysUser::getRealName, patientName)
                            .eq(SysUser::getRole, 3) // 只查询患者角色
            );
            List<Long> userIds = users.stream()
                    .map(SysUser::getId)
                    .collect(Collectors.toList());

            System.out.println("患者姓名：" + patientName + ", 找到的患者用户 IDs: " + userIds);

            if (!userIds.isEmpty()) {
                wrapper.in(FollowAppoint::getPatientId, userIds);
            } else {
                wrapper.eq(FollowAppoint::getPatientId, -1); // 无匹配
            }
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(FollowAppoint::getStatus, status);
        }

        if (StringUtils.hasText(appointTime)) {
            wrapper.eq(FollowAppoint::getAppointTime, appointTime);
        }

        // 按预约时间降序
        wrapper.orderByDesc(FollowAppoint::getAppointTime);

        long count = followAppointMapper.selectCount(wrapper);
        System.out.println("符合条件的记录数：" + count);

        Page<FollowAppoint> appointPage = followAppointMapper.selectPage(followPage, wrapper);

        System.out.println("查询到的记录：" + appointPage.getRecords().size());

        // 转换为 Map 格式并填充患者信息
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<FollowAppoint> appointments = appointPage.getRecords();

        if (!appointments.isEmpty()) {
            // 批量获取所有患者用户 ID（这些 ID 直接是 user.id）
            List<Long> patientUserIds = appointments.stream()
                    .map(FollowAppoint::getPatientId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            System.out.println("患者用户 ID 列表：" + patientUserIds);

            // 直接从 user 表查询患者信息
            Map<Long, SysUser> patientUserMap = new HashMap<>();
            if (!patientUserIds.isEmpty()) {
                List<SysUser> patients = sysUserMapper.selectBatchIds(patientUserIds);
                for (SysUser patient : patients) {
                    patientUserMap.put(patient.getId(), patient);
                }
            }

            // 构建返回结果
            for (FollowAppoint appoint : appointments) {
                Map<String, Object> appointMap = new HashMap<>();
                appointMap.put("id", appoint.getId());
                appointMap.put("patientId", appoint.getPatientId());
                appointMap.put("doctorId", appoint.getDoctorId());
                appointMap.put("appointTime", appoint.getAppointTime());
                appointMap.put("status", appoint.getStatus());
                appointMap.put("remark", appoint.getRemark());

                // 填充患者信息（直接从 user 表获取）
                SysUser patientUser = patientUserMap.get(appoint.getPatientId());
                if (patientUser != null) {
                    appointMap.put("patientName", patientUser.getRealName());
                    appointMap.put("patientPhone", patientUser.getPhone());
                } else {
                    appointMap.put("patientName", "未知患者");
                    appointMap.put("patientPhone", "-");
                }

                System.out.println("预约 ID: " + appoint.getId() + ", 患者：" + appointMap.get("patientName"));

                resultList.add(appointMap);
            }
        }

        // 创建新的 Page 对象
        Page<Map<String, Object>> resultPage = new Page<>(page, size);
        resultPage.setRecords(resultList);
        resultPage.setTotal(appointPage.getTotal());

        System.out.println("返回结果总数：" + resultPage.getTotal());

        return resultPage;
    }

    @Override
    public Map<String, Object> getDoctorStats(Long doctorId) {
        Map<String, Object> stats = new HashMap<>();

        System.out.println("=== 获取医生统计数据 ===");
        System.out.println("doctorId (user.id): " + doctorId);

        // 1. 统计预约数量（排除已取消的）
        LambdaQueryWrapper<FollowAppoint> appointWrapper = new LambdaQueryWrapper<>();
        appointWrapper.eq(FollowAppoint::getDoctorId, doctorId);
        appointWrapper.ne(FollowAppoint::getStatus, "已取消");
        Long appointCount = followAppointMapper.selectCount(appointWrapper);
        stats.put("appointCount", appointCount);
        System.out.println("预约数量：" + appointCount);

        // 2. 统计随访计划数量
        LambdaQueryWrapper<FollowPlan> planWrapper = new LambdaQueryWrapper<>();
        planWrapper.eq(FollowPlan::getDoctorId, doctorId);
        Long planCount = followPlanMapper.selectCount(planWrapper);
        stats.put("planCount", planCount);
        System.out.println("随访计划数量：" + planCount);

        // 3. 统计随访记录数量
        LambdaQueryWrapper<FollowRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(FollowRecord::getDoctorId, doctorId);
        Long recordCount = followRecordMapper.selectCount(recordWrapper);
        stats.put("recordCount", recordCount);
        System.out.println("随访记录数量：" + recordCount);

        return stats;
    }
}
