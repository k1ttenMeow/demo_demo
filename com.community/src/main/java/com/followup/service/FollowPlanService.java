package com.followup.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.followup.entity.FollowPlan;

public interface FollowPlanService extends IService<FollowPlan> {

    /**
     * 分页查询随访计划
     */
    Page<FollowPlan> getPlanList(Integer page, Integer size, String patientName, String doctorName, String planType, String status);

    /**
     * 创建随访计划
     */
    boolean createPlan(FollowPlan plan);

    /**
     * 更新随访计划
     */
    boolean updatePlan(FollowPlan plan);

    /**
     * 删除随访计划
     */
    boolean deletePlan(Long id);
}
