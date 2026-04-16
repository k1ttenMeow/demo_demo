package com.followup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.Patient;
import com.followup.entity.SysUser;
import com.followup.entity.Doctor;
import com.followup.mapper.PatientMapper;
import com.followup.service.PatientService;
import com.followup.mapper.SysUserMapper;
import com.followup.mapper.DoctorMapper;
import com.followup.vo.PatientDashboardVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Override
    public Page<Patient> getPatientList(Integer page, Integer size, String realName, String phone, String chronicType, String doctorName) {
        Page<Patient> patientPage = new Page<>(page, size);

        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();

        // 动态添加查询条件
        if (StringUtils.hasText(realName)) {
            // 先查询用户 ID
            List<SysUser> users = sysUserMapper.selectList(
                    new LambdaQueryWrapper<SysUser>()
                            .like(SysUser::getRealName, realName)
                            .eq(SysUser::getRole, 3) // 只查询患者角色
            );
            List<Long> userIds = users.stream()
                    .map(SysUser::getId)
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                wrapper.in(Patient::getUserId, userIds);
            } else {
                wrapper.eq(Patient::getUserId, -1); // 无匹配
            }
        }

        if (StringUtils.hasText(phone)) {
            // 先查询用户 ID
            List<SysUser> users = sysUserMapper.selectList(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getPhone, phone)
                            .eq(SysUser::getRole, 3)
            );
            List<Long> userIds = users.stream()
                    .map(SysUser::getId)
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                wrapper.in(Patient::getUserId, userIds);
            } else {
                wrapper.eq(Patient::getUserId, -1); // 无匹配
            }
        }

        if (StringUtils.hasText(chronicType)) {
            wrapper.eq(Patient::getChronicType, chronicType);
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
                wrapper.in(Patient::getDoctorId, doctorIds);
            } else {
                wrapper.eq(Patient::getDoctorId, -1); // 无匹配
            }
        }

        // 按创建时间降序
        wrapper.orderByDesc(Patient::getCreateTime);

        // 执行查询
        Page<Patient> result = patientMapper.selectPage(patientPage, wrapper);

        // 填充关联信息（用户名、手机号、医生姓名等）
        List<Patient> patients = result.getRecords();
        if (!patients.isEmpty()) {
            // 批量获取所有用户 ID
            List<Long> userIds = patients.stream()
                    .map(Patient::getUserId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量获取所有医生 ID
            List<Long> doctorIds = patients.stream()
                    .map(Patient::getDoctorId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            System.out.println("查询的用户 ID 列表：" + userIds);
            System.out.println("查询的医生 ID 列表：" + doctorIds);

            // 查询用户信息
            Map<Long, SysUser> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
                System.out.println("查询到的用户数量：" + users.size());
                for (SysUser user : users) {
                    System.out.println("用户 ID: " + user.getId() + ", 姓名：" + user.getRealName() + ", 手机号：" + user.getPhone());
                }
                userMap = users.stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u));
            }

            // 查询医生信息（需要关联查询 user 表获取医生姓名）
            Map<Long, String> doctorNameMap = new HashMap<>();
            if (!doctorIds.isEmpty()) {
                // 查询医生及其关联的用户信息
                for (Long doctorId : doctorIds) {
                    Doctor doctor = doctorMapper.selectById(doctorId);
                    if (doctor != null && doctor.getUserId() != null) {
                        SysUser doctorUser = sysUserMapper.selectById(doctor.getUserId());
                        if (doctorUser != null) {
                            doctorNameMap.put(doctorId, doctorUser.getRealName());
                            System.out.println("医生 ID: " + doctorId + ", 姓名：" + doctorUser.getRealName());
                        }
                    }
                }
            }

            // 填充每个患者的关联信息
            for (Patient patient : patients) {
                SysUser user = userMap.get(patient.getUserId());
                if (user != null) {
                    patient.setRealName(user.getRealName());
                    patient.setUsername(user.getUsername());
                    patient.setPhone(user.getPhone());
                    patient.setStatus(user.getStatus());
                    System.out.println("设置患者姓名：" + user.getRealName() + ", 手机号：" + user.getPhone());
                } else {
                    System.out.println("未找到用户 ID: " + patient.getUserId() + " 的信息");
                }

                String dName = doctorNameMap.get(patient.getDoctorId());
                if (dName != null) {
                    patient.setDoctorName(dName);
                    System.out.println("设置责任医生：" + dName);
                }
            }
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean createPatient(Patient patient) {
        System.out.println("=== 开始创建患者 ===");
        System.out.println("username: " + patient.getUsername());
        System.out.println("realName: " + patient.getRealName());
        System.out.println("phone: " + patient.getPhone());
        System.out.println("doctorId: " + patient.getDoctorId());

        // 1. 创建用户信息（user 表）
        SysUser user = new SysUser();
        user.setUsername(patient.getUsername());
        user.setPassword("{bcrypt}" + new BCryptPasswordEncoder().encode(patient.getPassword()));
        user.setRealName(patient.getRealName());
        user.setPhone(patient.getPhone());
        user.setRole(3); // 患者角色
        user.setStatus(patient.getStatus() != null ? patient.getStatus() : 1);

        int userResult = sysUserMapper.insert(user);
        System.out.println("插入 user 表结果：" + userResult + ", userId: " + user.getId());

        if (userResult > 0 && user.getId() != null) {
            // 2. 创建患者信息（patient 表）
            patient.setUserId(user.getId());

            // 根据手机号自动设置性别（简单规则：奇数男，偶数女）
            if (patient.getGender() == null && patient.getPhone() != null) {
                String lastDigit = patient.getPhone().substring(patient.getPhone().length() - 1);
                patient.setGender(Integer.parseInt(lastDigit) % 2 == 1 ? 1 : 2);
            }

            int patientResult = patientMapper.insert(patient);
            System.out.println("插入 patient 表结果：" + patientResult);
            return patientResult > 0;
        }

        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updatePatient(Patient patient) {
        System.out.println("=== 开始更新患者 ===");
        System.out.println("userId: " + patient.getUserId());
        System.out.println("doctorId: " + patient.getDoctorId());

        // 1. 更新用户信息（user 表）
        if (patient.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(patient.getUserId());
            if (user != null) {
                user.setRealName(patient.getRealName());
                user.setPhone(patient.getPhone());
                user.setStatus(patient.getStatus() != null ? patient.getStatus() : 1);

                // 如果填写了密码则更新密码
                if (StringUtils.hasText(patient.getPassword())) {
                    user.setPassword("{bcrypt}" + new BCryptPasswordEncoder().encode(patient.getPassword()));
                }

                int userResult = sysUserMapper.updateById(user);
                System.out.println("更新 user 表结果：" + userResult);
            }
        }

        // 2. 更新患者信息（patient 表）
        // 根据手机号自动设置性别
        if (patient.getGender() == null && patient.getPhone() != null) {
            String lastDigit = patient.getPhone().substring(patient.getPhone().length() - 1);
            patient.setGender(Integer.parseInt(lastDigit) % 2 == 1 ? 1 : 2);
        }

        int patientResult = patientMapper.updateById(patient);
        System.out.println("更新 patient 表结果：" + patientResult);

        return patientResult > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deletePatient(Long id) {
        int result = patientMapper.deleteById(id);
        return result > 0;
    }

    @Override
    public PatientDashboardVO getDashboardInfo(Long patientId) {
        // 查询患者基本信息
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            return null;
        }

        PatientDashboardVO vo = new PatientDashboardVO();
        vo.setPatientId(patient.getId());
        vo.setUserId(patient.getUserId());
        vo.setChronicType(patient.getChronicType());
        vo.setAge(patient.getAge());
        vo.setAddress(patient.getAddress());

        // 查询用户信息（姓名、手机号）
        if (patient.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(patient.getUserId());
            if (user != null) {
                vo.setRealName(user.getRealName());
                vo.setPhone(user.getPhone());
            }
        }

        // 查询责任医生姓名
        if (patient.getDoctorId() != null) {
            Doctor doctor = doctorMapper.selectById(patient.getDoctorId());
            if (doctor != null && doctor.getUserId() != null) {
                SysUser doctorUser = sysUserMapper.selectById(doctor.getUserId());
                if (doctorUser != null) {
                    vo.setDoctorName(doctorUser.getRealName());
                }
            }
        }

        return vo;
    }
}
