package com.followup.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.common.R;
import com.followup.entity.Patient;
import com.followup.service.PatientService;
import com.followup.vo.PatientDashboardVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private static final Logger log = LoggerFactory.getLogger(PatientController.class);

    @Resource
    private PatientService patientService;

    /**
     * 分页查询患者列表
     */
    @GetMapping("/list")
    public R<Page<Patient>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String chronicType,
            @RequestParam(required = false) String doctorName
    ) {
        try {
            Page<Patient> patientPage = patientService.getPatientList(page, size, realName, phone, chronicType, doctorName);
            return R.success(patientPage);
        } catch (Exception e) {
            log.error("查询患者列表异常", e);
            return R.error("查询失败");
        }
    }

    /**
     * 获取患者仪表板信息
     */
    @GetMapping("/dashboard/{patientId}")
    public R<PatientDashboardVO> getDashboardInfo(@PathVariable Long patientId) {
        try {
            PatientDashboardVO vo = patientService.getDashboardInfo(patientId);
            if (vo != null) {
                return R.success(vo);
            } else {
                return R.error("患者不存在");
            }
        } catch (Exception e) {
            log.error("获取患者仪表板信息异常", e);
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
