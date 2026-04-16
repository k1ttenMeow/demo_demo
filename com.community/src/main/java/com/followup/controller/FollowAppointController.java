package com.followup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.common.R;
import com.followup.entity.Doctor;
import com.followup.entity.FollowAppoint;
import com.followup.entity.SysUser;
import com.followup.mapper.DoctorMapper;
import com.followup.mapper.SysUserMapper;
import com.followup.service.FollowAppointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/followup/appoint")
public class FollowAppointController {

    private static final Logger log = LoggerFactory.getLogger(FollowAppointController.class);

    @Resource
    private FollowAppointService followAppointService;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private DoctorMapper doctorMapper;

    /**
     * 分页查询随访预约列表
     */
    @GetMapping("/list")
    public R<Page<FollowAppoint>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) String status
    ) {
        try {
            Page<FollowAppoint> followPage = followAppointService.getAppointList(page, size, patientName, doctorName, status);
            return R.success(followPage);
        } catch (Exception e) {
            log.error("查询随访预约列表异常", e);
            return R.error("查询失败");
        }
    }

    /**
     * 获取患者的预约列表（患者端专用）
     */
    @GetMapping("/my/{patientId}")
    public R<Page<Map<String, Object>>> getMyAppoints(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String appointTime
    ) {
        try {
            System.out.println("=== 查询患者预约列表 ===");
            System.out.println("patientId (user.id): " + patientId);

            Page<FollowAppoint> appointPage = new Page<>(page, size);

            LambdaQueryWrapper<FollowAppoint> wrapper = new LambdaQueryWrapper<>();
            // ✅ patient_id 直接对应 user.id
            wrapper.eq(FollowAppoint::getPatientId, patientId);

            // 动态添加查询条件
            if (status != null && !status.isEmpty()) {
                wrapper.eq(FollowAppoint::getStatus, status);
            }

            if (appointTime != null && !appointTime.isEmpty()) {
                wrapper.eq(FollowAppoint::getAppointTime, appointTime);
            }

            wrapper.orderByDesc(FollowAppoint::getAppointTime);

            Page<FollowAppoint> result = followAppointService.page(appointPage, wrapper);

            System.out.println("查询到的原始记录数：" + result.getRecords().size());
            System.out.println("总记录数：" + result.getTotal());

            // 打印每条记录的详细信息
            result.getRecords().forEach(appoint -> {
                System.out.println("  - 预约ID: " + appoint.getId() +
                        ", patientId: " + appoint.getPatientId() +
                        ", doctorId: " + appoint.getDoctorId() +
                        ", 时间: " + appoint.getAppointTime() +
                        ", 状态: " + appoint.getStatus());
            });

            // 转换为 Map 并填充医生信息
            List<Map<String, Object>> resultList = result.getRecords().stream().map(appoint -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", appoint.getId());
                map.put("patientId", appoint.getPatientId());
                map.put("doctorId", appoint.getDoctorId());
                map.put("appointTime", appoint.getAppointTime());
                map.put("status", appoint.getStatus());
                map.put("remark", appoint.getRemark());

                // ✅ doctor_id 对应的是 doctor 表的主键 id，需要先查 doctor 表
                if (appoint.getDoctorId() != null) {
                    Doctor doctor = doctorMapper.selectById(appoint.getDoctorId());
                    if (doctor != null && doctor.getUserId() != null) {
                        SysUser doctorUser = sysUserMapper.selectById(doctor.getUserId());
                        if (doctorUser != null) {
                            map.put("doctorName", doctorUser.getRealName());
                            System.out.println("  - 医生姓名: " + doctorUser.getRealName());
                        } else {
                            map.put("doctorName", null);
                        }
                    } else {
                        map.put("doctorName", null);
                    }
                } else {
                    map.put("doctorName", null);
                }

                return map;
            }).collect(Collectors.toList());

            Page<Map<String, Object>> resultPage = new Page<>(page, size);
            resultPage.setRecords(resultList);
            resultPage.setTotal(result.getTotal());

            System.out.println("最终返回的预约数量：" + resultList.size());

            return R.success(resultPage);
        } catch (Exception e) {
            log.error("查询患者预约列表异常", e);
            return R.error("查询失败");
        }
    }

    /**
     * 创建随访预约
     */
    @PostMapping
    public R<Boolean> create(@RequestBody FollowAppoint appoint) {
        try {
            System.out.println("=== 创建随访预约 ===");
            System.out.println("预约数据：" + appoint);

            boolean result = followAppointService.createAppoint(appoint);
            return R.success(result);
        } catch (Exception e) {
            log.error("创建随访预约异常", e);
            return R.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新随访预约
     */
    @PutMapping
    public R<Boolean> update(@RequestBody FollowAppoint appoint) {
        try {
            boolean result = followAppointService.updateAppoint(appoint);
            return R.success(result);
        } catch (Exception e) {
            log.error("更新随访预约异常", e);
            return R.error("更新失败");
        }
    }

    /**
     * 取消预约
     */
    @PutMapping("/{id}/cancel")
    public R<Boolean> cancelAppoint(@PathVariable Long id) {
        try {
            System.out.println("=== 取消预约 ===");
            System.out.println("预约ID：" + id);

            FollowAppoint appoint = followAppointService.getById(id);
            if (appoint == null) {
                return R.error("预约不存在");
            }

            appoint.setStatus("已取消");
            boolean result = followAppointService.updateById(appoint);
            return R.success(result);
        } catch (Exception e) {
            log.error("取消预约异常", e);
            return R.error("取消失败");
        }
    }

    /**
     * 完成预约
     */
    @PutMapping("/{id}/complete")
    public R<Boolean> completeAppoint(@PathVariable Long id) {
        try {
            System.out.println("=== 完成预约 ===");
            System.out.println("预约ID：" + id);

            FollowAppoint appoint = followAppointService.getById(id);
            if (appoint == null) {
                return R.error("预约不存在");
            }

            appoint.setStatus("已完成");
            boolean result = followAppointService.updateById(appoint);
            return R.success(result);
        } catch (Exception e) {
            log.error("完成预约异常", e);
            return R.error("操作失败");
        }
    }

    /**
     * 删除随访预约
     */
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        try {
            boolean result = followAppointService.deleteAppoint(id);
            return R.success(result);
        } catch (Exception e) {
            log.error("删除随访预约异常", e);
            return R.error("删除失败");
        }
    }
}
