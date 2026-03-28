package com.followup.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.followup.entity.FollowPlan;

public interface FollowPlanService extends IService<FollowPlan> {

    /**
     * 分页查询随访计划列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param patientName 患者姓名（可选）
     * @param doctorName 医生姓名（可选）
     * @param planType 计划类型（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    Page<FollowPlan> getPlanList(Integer page, Integer size, String patientName, String doctorName, String planType, String status);

    /**
     * 创建随访计划
     *
     * @param plan 随访计划实体
     * @return 是否成功
     */
    boolean createPlan(FollowPlan plan);

    /**
     * 更新随访计划
     *
     * @param plan 随访计划实体
     * @return 是否成功
     */
    boolean updatePlan(FollowPlan plan);

    /**
     * 删除随访计划
     *
     * @param id 计划 ID
     * @return 是否成功
     */
    boolean deletePlan(Long id);
}
