package com.followup.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.common.R;
import com.followup.entity.FollowPlan;
import com.followup.service.FollowPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/followup/plan")
public class FollowPlanController {

    private static final Logger log = LoggerFactory.getLogger(FollowPlanController.class);

    @Resource
    private FollowPlanService followPlanService;

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
            @RequestParam(required = false) String status
    ) {
        try {
            Page<FollowPlan> followPage = followPlanService.getPlanList(page, size, patientName, doctorName, planType, status);
            return R.success(followPage);
        } catch (Exception e) {
            log.error("查询随访计划列表异常", e);
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
}
