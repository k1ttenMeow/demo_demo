package com.followup.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.common.R;
import com.followup.entity.Doctor;
import com.followup.entity.SysUser;
import com.followup.mapper.SysUserMapper;
import com.followup.service.DoctorService;
import com.followup.vo.DoctorDashboardVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/doctor")
public class DoctorController {

    private static final Logger log = LoggerFactory.getLogger(DoctorController.class);

    @Resource
    private DoctorService doctorService;

    @Resource
    private SysUserMapper sysUserMapper;

    /**
     * 分页查询医生列表（返回包含用户信息的 Map 格式）
     */
    @GetMapping("/list")
    public R<Page<Map<String, Object>>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String community
    ) {
        try {
            // 调用原有方法获取 Doctor 列表
            Page<Doctor> doctorPage = doctorService.getDoctorList(page, size, realName, department, community);

            // 转换为 Map 格式并填充用户信息
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<Doctor> doctors = doctorPage.getRecords();

            if (!doctors.isEmpty()) {
                // 批量获取所有用户 ID
                List<Long> userIds = doctors.stream()
                        .map(Doctor::getUserId)
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
                for (Doctor doctor : doctors) {
                    Map<String, Object> doctorMap = new HashMap<>();
                    doctorMap.put("id", doctor.getId());
                    doctorMap.put("userId", doctor.getUserId());
                    doctorMap.put("gender", doctor.getGender());
                    doctorMap.put("department", doctor.getDepartment());
                    doctorMap.put("skill", doctor.getSkill());
                    doctorMap.put("community", doctor.getCommunity());
                    doctorMap.put("title", doctor.getTitle());
                    doctorMap.put("isOnline", doctor.getIsOnline());

                    // 填充用户信息
                    SysUser user = userMap.get(doctor.getUserId());
                    if (user != null) {
                        doctorMap.put("realName", user.getRealName());
                        doctorMap.put("username", user.getUsername());
                        doctorMap.put("phone", user.getPhone());
                        doctorMap.put("status", user.getStatus());
                    } else {
                        doctorMap.put("realName", null);
                        doctorMap.put("username", null);
                        doctorMap.put("phone", null);
                        doctorMap.put("status", null);
                    }

                    resultList.add(doctorMap);
                }
            }

            // 创建新的 Page 对象
            Page<Map<String, Object>> resultPage = new Page<>(page, size);
            resultPage.setRecords(resultList);
            resultPage.setTotal(doctorPage.getTotal());

            return R.success(resultPage);
        } catch (Exception e) {
            log.error("查询医生列表异常", e);
            return R.error("查询失败");
        }
    }

    /**
     * 获取医生仪表板信息
     */
    @GetMapping("/dashboard/{doctorId}")
    public R<DoctorDashboardVO> getDashboardInfo(@PathVariable Long doctorId) {
        try {
            DoctorDashboardVO vo = doctorService.getDashboardInfo(doctorId);
            if (vo != null) {
                return R.success(vo);
            } else {
                return R.error("医生不存在");
            }
        } catch (Exception e) {
            log.error("获取医生仪表板信息异常", e);
            return R.error("查询失败");
        }
    }

    /**
     * 创建医生信息
     */
    @PostMapping
    public R<Boolean> create(@RequestBody Doctor doctor) {
        try {
            boolean result = doctorService.createDoctor(doctor);
            return R.success(result);
        } catch (Exception e) {
            log.error("创建医生信息异常", e);
            return R.error("创建失败");
        }
    }

    /**
     * 更新医生信息
     */
    @PutMapping
    public R<Boolean> update(@RequestBody Doctor doctor) {
        try {
            boolean result = doctorService.updateDoctor(doctor);
            return R.success(result);
        } catch (Exception e) {
            log.error("更新医生信息异常", e);
            return R.error("更新失败");
        }
    }

    /**
     * 删除医生信息
     */
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        try {
            boolean result = doctorService.deleteDoctor(id);
            return R.success(result);
        } catch (Exception e) {
            log.error("删除医生信息异常", e);
            return R.error("删除失败");
        }
    }
}
