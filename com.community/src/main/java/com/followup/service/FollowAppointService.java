package com.followup.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.followup.entity.FollowAppoint;

import java.util.Map;

public interface FollowAppointService extends IService<FollowAppoint> {

    /**
     * 分页查询随访预约
     */
    Page<FollowAppoint> getAppointList(Integer page, Integer size, String patientName, String doctorName, String status);

    /**
     * 创建随访预约
     */
    boolean createAppoint(FollowAppoint appoint);

    /**
     * 更新随访预约
     */
    boolean updateAppoint(FollowAppoint appoint);

    /**
     * 删除随访预约
     */
    boolean deleteAppoint(Long id);
}
