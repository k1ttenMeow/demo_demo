package com.followup.service.impl;

import com.followup.entity.SysUser;
import com.followup.mapper.DoctorMapper;
import com.followup.entity.Doctor;
import com.followup.mapper.FollowupRecordMapper;
import com.followup.mapper.SysUserMapper;
import com.followup.service.AdminService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private FollowupRecordMapper followMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUser", sysUserMapper.totalUser() != null ? sysUserMapper.totalUser() : 0);
        stats.put("doctorCount", sysUserMapper.doctorCount() != null ? sysUserMapper.doctorCount() : 0);
        stats.put("patientCount", sysUserMapper.patientCount() != null ? sysUserMapper.patientCount() : 0);
        stats.put("followCount", followMapper.followCount() != null ? followMapper.followCount() : 0);
        return stats;
    }

    @Override
    public List<SysUser> getRecentUser() {
        List<SysUser> users = sysUserMapper.recentUser();
        return users != null ? users : new ArrayList<>();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean registerUser(SysUser user) {
        // 检查用户名是否已存在
        SysUser existUser = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, user.getUsername())
        );
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getUserType() == null) {
            user.setUserType(3); // 默认为患者
        }

        // 插入用户表
        int result = sysUserMapper.insert(user);
        if (result <= 0) {
            throw new RuntimeException("创建用户失败");
        }

        // 如果是医生，同时创建医生记录
        if (user.getUserType() == 2) {
            Doctor doctor = new Doctor();
            doctor.setUserId(user.getId()); // 关联用户 ID
            doctor.setDepartment(user.getDepartment());
            doctor.setSkill(user.getSkill());
            doctor.setCommunity(user.getCommunity());

            int doctorResult = doctorMapper.insert(doctor);
            if (doctorResult <= 0) {
                throw new RuntimeException("创建医生信息失败");
            }
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateUser(SysUser user) {
        return sysUserMapper.updateById(user) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteUser(Long id) {
        try {
            // 先查询用户信息
            SysUser user = sysUserMapper.selectById(id);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 如果是医生，先删除医生表记录
            if (user.getUserType() == 2) {
                Doctor doctor = doctorMapper.selectOne(
                        new LambdaQueryWrapper<Doctor>()
                                .eq(Doctor::getUserId, id)
                );
                if (doctor != null) {
                    doctorMapper.deleteById(doctor.getId());
                }
            }

            // 再删除用户表记录
            return sysUserMapper.deleteById(id) > 0;
        } catch (Exception e) {
            throw new RuntimeException("删除用户失败：" + e.getMessage());
        }
    }
}
