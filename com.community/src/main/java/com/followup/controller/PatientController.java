package com.followup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.entity.Patient;
import com.followup.service.PatientService;
import com.followup.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 患者模块 控制器
 *
 * @author system
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/patient")
public class PatientController {

    @Resource
    private PatientService patientService;

    @PostMapping("/add")
    public ResultVO<Boolean> addPatient(@RequestBody Patient patient) {
        return ResultVO.success(patientService.save(patient));
    }

    @DeleteMapping("/delete/{id}")
    public ResultVO<Boolean> deletePatient(@PathVariable Long id) {
        return ResultVO.success(patientService.removeById(id));
    }

    @PutMapping("/update")
    public ResultVO<Boolean> updatePatient(@RequestBody Patient patient) {
        return ResultVO.success(patientService.updateById(patient));
    }

    @GetMapping("/info/{id}")
    public ResultVO<Patient> getPatientInfo(@PathVariable Long id) {
        return ResultVO.success(patientService.getById(id));
    }

    @GetMapping("/page")
    public ResultVO<Page<Patient>> getPatientPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Patient> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Patient::getCreateTime);
        return ResultVO.success(patientService.page(page, wrapper));
    }
}