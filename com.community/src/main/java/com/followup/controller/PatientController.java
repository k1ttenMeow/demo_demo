package com.followup.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.common.R;
import com.followup.entity.Doctor;
import com.followup.entity.Patient;
import com.followup.entity.SysUser;
import com.followup.mapper.DoctorMapper;
import com.followup.mapper.SysUserMapper;
import com.followup.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private static final Logger log = LoggerFactory.getLogger(PatientController.class);

    @Resource
    private PatientService patientService;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private DoctorMapper doctorMapper;

    /**
     * 获取患者仪表板信息（个人详细信息）
     */
    @GetMapping("/dashboard/{patientId}")
    public R<Map<String, Object>> getDashboardInfo(@PathVariable Long patientId) {
        try {
            System.out.println("=== 获取患者仪表板信息 ===");
            System.out.println("patientId (user.id): " + patientId);

            // 1. 查询用户信息
            SysUser user = sysUserMapper.selectById(patientId);
            if (user == null || user.getUserType() != 3) {
                System.out.println("用户不存在或不是患者角色");
                return R.error("患者不存在");
            }

            // 2. 通过 user_id 查找 patient 记录
            List<Patient> patients = patientService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Patient>()
                            .eq(Patient::getUserId, patientId)
            );

            if (patients.isEmpty()) {
                System.out.println("未找到对应的患者档案");
                return R.error("未找到患者档案");
            }

            Patient patient = patients.get(0);
            System.out.println("找到患者档案，patient.id: " + patient.getId());

            // 3. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("id", patient.getId());
            result.put("userId", patient.getUserId());
            result.put("gender", patient.getGender());
            result.put("doctorId", patient.getDoctorId());
            result.put("chronicType", patient.getChronicType());
            result.put("age", patient.getAge());
            result.put("address", patient.getAddress());
            result.put("emergencyContact", patient.getEmergencyContact());
            result.put("emergencyPhone", patient.getEmergencyPhone());

            // 填充用户信息
            result.put("realName", user.getRealName());
            result.put("phone", user.getPhone());
            result.put("username", user.getUsername());
            result.put("status", user.getStatus());

            // 4. 查询责任医生姓名
            if (patient.getDoctorId() != null) {
                // patient.doctor_id 对应的是 doctor 表的主键 id
                Doctor doctor = doctorMapper.selectById(patient.getDoctorId());
                if (doctor != null && doctor.getUserId() != null) {
                    SysUser doctorUser = sysUserMapper.selectById(doctor.getUserId());
                    if (doctorUser != null) {
                        result.put("doctorName", doctorUser.getRealName());
                        System.out.println("✅ 责任医生：" + doctorUser.getRealName());
                    } else {
                        result.put("doctorName", null);
                    }
                } else {
                    result.put("doctorName", null);
                }
            } else {
                result.put("doctorName", null);
            }

            System.out.println("返回的患者信息：" + result);
            return R.success(result);
        } catch (Exception e) {
            log.error("获取患者仪表板信息异常", e);
            return R.error("查询失败");
        }
    }

    /**
     * 分页查询患者列表
     */
    @GetMapping("/list")
    public R<Page<Map<String, Object>>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String chronicType,
            @RequestParam(required = false) String community
    ) {
        try {
            // 添加 null 作为第 6 个参数（doctorName）
            Page<Patient> patientPage = patientService.getPatientList(page, size, realName, chronicType, community, null);

            // 为每个患者添加真实姓名和手机号（从 user 表）
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Patient patient : patientPage.getRecords()) {
                Map<String, Object> patientMap = new HashMap<>();
                patientMap.put("id", patient.getId());
                patientMap.put("userId", patient.getUserId());
                patientMap.put("gender", patient.getGender());
                patientMap.put("doctorId", patient.getDoctorId());
                patientMap.put("chronicType", patient.getChronicType());
                patientMap.put("age", patient.getAge());
                patientMap.put("address", patient.getAddress());
                patientMap.put("emergencyContact", patient.getEmergencyContact());
                patientMap.put("emergencyPhone", patient.getEmergencyPhone());

                // 查询用户信息获取真实姓名和手机号
                if (patient.getUserId() != null) {
                    SysUser user = sysUserMapper.selectById(patient.getUserId());
                    if (user != null) {
                        patientMap.put("realName", user.getRealName());
                        patientMap.put("phone", user.getPhone());
                    } else {
                        patientMap.put("realName", null);
                        patientMap.put("phone", null);
                    }
                } else {
                    patientMap.put("realName", null);
                    patientMap.put("phone", null);
                }

                resultList.add(patientMap);
            }

            // 创建新的 Page 对象，包含转换后的数据
            Page<Map<String, Object>> resultPage = new Page<>(page, size);
            resultPage.setRecords(resultList);
            resultPage.setTotal(patientPage.getTotal());

            return R.success(resultPage);
        } catch (Exception e) {
            log.error("查询患者列表异常", e);
            return R.error("查询失败");
        }
    }

    /**
     * 创建患者信息
     */
    @PostMapping
    public R<Boolean> create(@RequestBody Patient patient) {
        try {
            boolean result = patientService.createPatient(patient);
            return R.success(result);
        } catch (Exception e) {
            log.error("创建患者信息异常", e);
            return R.error("创建失败");
        }
    }

    /**
     * 更新患者信息
     */
    @PutMapping
    public R<Boolean> update(@RequestBody Patient patient) {
        try {
            System.out.println("=== 开始更新患者信息 ===");
            System.out.println("接收到的数据：" + patient);

            boolean result = patientService.updatePatient(patient);
            return R.success(result);
        } catch (Exception e) {
            log.error("更新患者信息异常", e);
            return R.error("更新失败：" + e.getMessage());
        }
    }


    /**
     * 删除患者信息
     */
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        try {
            boolean result = patientService.deletePatient(id);
            return R.success(result);
        } catch (Exception e) {
            log.error("删除患者信息异常", e);
            return R.error("删除失败");
        }
    }
}
