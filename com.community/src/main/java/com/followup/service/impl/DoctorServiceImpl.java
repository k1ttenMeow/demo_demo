package com.followup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.Doctor;
import com.followup.entity.SysUser;
import com.followup.entity.Patient;
import com.followup.mapper.DoctorMapper;
import com.followup.mapper.SysUserMapper;
import com.followup.mapper.PatientMapper;
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
                            .eq(SysUser::getUserType, 2) // 只查询医生角色
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
        user.setUserType(2); // 医生角色
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
        // 查询医生基本信息
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
    public Map<String, Object> getStats(Long doctorId) {
        Map<String, Object> stats = new HashMap<>();

        // 查询医生管理的患者数量
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Patient::getDoctorId, doctorId);
        Long patientCount = patientMapper.selectCount(wrapper);

        stats.put("patientCount", patientCount);

        return stats;
    }

    @Override
    public Page<Map<String, Object>> getPatientList(Long doctorId, Integer page, Integer size) {
        Page<Patient> patientPage = new Page<>(page, size);

        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Patient::getDoctorId, doctorId);
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

                // 填充用户信息
                SysUser user = userMap.get(patient.getUserId());
                if (user != null) {
                    patientMap.put("realName", user.getRealName());
                    patientMap.put("phone", user.getPhone());
                } else {
                    patientMap.put("realName", null);
                    patientMap.put("phone", null);
                }

                resultList.add(patientMap);
            }
        }

        // 创建新的 Page 对象
        Page<Map<String, Object>> resultPage = new Page<>(page, size);
        resultPage.setRecords(resultList);
        resultPage.setTotal(result.getTotal());

        return resultPage;
    }
}
