package com.followup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.common.R;
import com.followup.entity.Doctor;
import com.followup.entity.FollowPlan;
import com.followup.entity.SysUser;
import com.followup.mapper.DoctorMapper;
import com.followup.mapper.SysUserMapper;
import com.followup.service.FollowPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/followup/plan")
public class FollowPlanController {

    private static final Logger log = LoggerFactory.getLogger(FollowPlanController.class);

    @Resource
    private FollowPlanService followPlanService;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private DoctorMapper doctorMapper;

    /**
     * 分页查询随访计划列表
     */
    @GetMapping("/list")
    public R<Page<FollowPlan>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long doctorId
    ) {
        try {
            Page<FollowPlan> followPage = followPlanService.getPlanList(page, size, patientName, doctorName, planType, status, doctorId);
            return R.success(followPage);
        } catch (Exception e) {
            log.error("查询随访计划列表异常", e);
            return R.error("查询失败");
        }
    }

    /**
     * 获取患者的随访计划列表（患者端专用）
     */
    @GetMapping("/my/{patientId}")
    public R<Page<Map<String, Object>>> getMyPlans(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status
    ) {
        try {
            System.out.println("=== 查询患者随访计划列表 ===");
            System.out.println("patientId (user.id): " + patientId);

            Page<FollowPlan> planPage = new Page<>(page, size);

            LambdaQueryWrapper<FollowPlan> wrapper = new LambdaQueryWrapper<>();
            // ✅ patient_id 直接对应 user.id
            wrapper.eq(FollowPlan::getPatientId, patientId);

            // 动态添加查询条件
            if (status != null && !status.isEmpty()) {
                wrapper.eq(FollowPlan::getStatus, status);
            }

            wrapper.orderByDesc(FollowPlan::getCreateTime);

            Page<FollowPlan> result = followPlanService.page(planPage, wrapper);

            System.out.println("查询到的原始记录数：" + result.getRecords().size());
            System.out.println("总记录数：" + result.getTotal());

            // 打印每条记录的详细信息
            result.getRecords().forEach(plan -> {
                System.out.println("  - 计划ID: " + plan.getId() +
                        ", patientId: " + plan.getPatientId() +
                        ", doctorId: " + plan.getDoctorId() +
                        ", 计划类型: " + plan.getPlanType() +
                        ", 周期: " + plan.getCycle() +
                        ", 下次随访: " + plan.getNextTime() +
                        ", 状态: " + plan.getStatus());
            });

            // 转换为 Map 并填充医生信息
            List<Map<String, Object>> resultList = result.getRecords().stream().map(plan -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", plan.getId());
                map.put("patientId", plan.getPatientId());
                map.put("doctorId", plan.getDoctorId());
                map.put("planType", plan.getPlanType());
                map.put("cycle", plan.getCycle());
                map.put("nextTime", plan.getNextTime());
                map.put("status", plan.getStatus());
                map.put("remark", plan.getRemark());

                // ✅ doctor_id 直接对应 user.id，直接查询用户表获取医生姓名
                if (plan.getDoctorId() != null) {
                    SysUser doctorUser = sysUserMapper.selectById(plan.getDoctorId());
                    if (doctorUser != null) {
                        map.put("doctorName", doctorUser.getRealName());
                        System.out.println("  - 医生姓名: " + doctorUser.getRealName());
                    }
                }

                return map;
            }).collect(Collectors.toList());

            Page<Map<String, Object>> resultPage = new Page<>(page, size);
            resultPage.setRecords(resultList);
            resultPage.setTotal(result.getTotal());

            System.out.println("最终返回的计划数量：" + resultList.size());

            return R.success(resultPage);
        } catch (Exception e) {
            log.error("查询患者随访计划列表异常", e);
            return R.error("查询失败");
        }
    }


    /**
     * 创建随访计划
     */
    @PostMapping
    public R<Boolean> create(@RequestBody FollowPlan plan) {
        try {
            boolean result = followPlanService.createPlan(plan);
            return R.success(result);
        } catch (Exception e) {
            log.error("创建随访计划异常", e);
            return R.error("创建失败");
        }
    }

    /**
     * 更新随访计划
     */
    @PutMapping
    public R<Boolean> update(@RequestBody FollowPlan plan) {
        try {
            boolean result = followPlanService.updatePlan(plan);
            return R.success(result);
        } catch (Exception e) {
            log.error("更新随访计划异常", e);
            return R.error("更新失败");
        }
    }

    /**
     * 删除随访计划
     */
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        try {
            boolean result = followPlanService.deletePlan(id);
            return R.success(result);
        } catch (Exception e) {
            log.error("删除随访计划异常", e);
            return R.error("删除失败");
        }
    }

    /**
     * 修改随访计划状态
     */
    @PutMapping("/status")
    public R<Boolean> updateStatus(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            String status = (String) params.get("status");

            boolean result = followPlanService.updatePlanStatus(id, status);
            return R.success(result);
        } catch (Exception e) {
            log.error("修改随访计划状态异常", e);
            return R.error("修改失败");
        }
    }
}
