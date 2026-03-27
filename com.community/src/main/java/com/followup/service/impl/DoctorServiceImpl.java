package com.followup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.Doctor;
import com.followup.entity.SysUser;
import com.followup.mapper.DoctorMapper;
import com.followup.mapper.SysUserMapper;
import com.followup.service.DoctorService;
import com.followup.vo.DoctorDashboardVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
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

    @Override
    public Page<Doctor> getDoctorList(Integer page, Integer size, String realName, String department, String community) {
        Page<Doctor> doctorPage = new Page<>(page, size);

        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();

        // 动态添加查询条件
        if (StringUtils.hasText(realName)) {
            // 先查询用户 ID（通过真实姓名）
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

        // 执行查询
        Page<Doctor> result = this.page(doctorPage, wrapper);

        // 填充关联信息（用户名、手机号、状态等）
        List<Doctor> doctors = result.getRecords();
        if (!doctors.isEmpty()) {
            List<Long> userIds = doctors.stream()
                    .map(Doctor::getUserId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, SysUser> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
                userMap = users.stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u));
            }

            for (Doctor doctor : doctors) {
                SysUser user = userMap.get(doctor.getUserId());
                if (user != null) {
                    doctor.setRealName(user.getRealName());
                    doctor.setUsername(user.getUsername());
                    doctor.setPhone(user.getPhone());
                    doctor.setStatus(user.getStatus());
                }
            }
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean createDoctor(Doctor doctor) {
        int result = doctorMapper.insert(doctor);
        return result > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateDoctor(Doctor doctor) {
        int result = doctorMapper.updateById(doctor);
        return result > 0;
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
}
