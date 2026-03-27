package com.followup.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.common.R;
import com.followup.entity.Doctor;
import com.followup.service.DoctorService;
import com.followup.vo.DoctorDashboardVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/doctor")
public class DoctorController {

    private static final Logger log = LoggerFactory.getLogger(DoctorController.class);

    @Resource
    private DoctorService doctorService;

    /**
     * 分页查询医生列表
     */
    @GetMapping("/list")
    public R<Page<Doctor>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String community
    ) {
        try {
            Page<Doctor> doctorPage = doctorService.getDoctorList(page, size, realName, department, community);
            return R.success(doctorPage);
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
