package com.followup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.entity.Doctor;
import com.followup.service.DoctorService;
import com.followup.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 医生模块 控制器
 *
 * @author system
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/doctor")
public class DoctorController {

    @Resource
    private DoctorService doctorService;

    @PostMapping("/add")
    public ResultVO<Boolean> addDoctor(@RequestBody Doctor doctor) {
        return ResultVO.success(doctorService.save(doctor));
    }

    @DeleteMapping("/delete/{id}")
    public ResultVO<Boolean> deleteDoctor(@PathVariable Long id) {
        return ResultVO.success(doctorService.removeById(id));
    }

    @PutMapping("/update")
    public ResultVO<Boolean> updateDoctor(@RequestBody Doctor doctor) {
        return ResultVO.success(doctorService.updateById(doctor));
    }

    @GetMapping("/info/{id}")
    public ResultVO<Doctor> getDoctorInfo(@PathVariable Long id) {
        return ResultVO.success(doctorService.getById(id));
    }

    @GetMapping("/page")
    public ResultVO<Page<Doctor>> getDoctorPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Doctor> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
       // wrapper.orderByDesc(Doctor::getCreateTime);
        return ResultVO.success(doctorService.page(page, wrapper));
    }
}