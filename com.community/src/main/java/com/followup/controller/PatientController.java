package com.followup.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.common.R;
import com.followup.entity.Patient;
import com.followup.entity.SysUser;
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
            boolean result = patientService.updatePatient(patient);
            return R.success(result);
        } catch (Exception e) {
            log.error("更新患者信息异常", e);
            return R.error("更新失败");
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
