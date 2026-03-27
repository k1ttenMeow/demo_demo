package com.followup.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.followup.entity.FollowRecord;

public interface FollowRecordService extends IService<FollowRecord> {

    /**
     * 分页查询随访记录
     */
    Page<FollowRecord> getRecordList(Integer page, Integer size, String patientName, String doctorName, String startDate, String endDate);

    /**
     * 创建随访记录
     */
    boolean createRecord(FollowRecord record);

    /**
     * 更新随访记录
     */
    boolean updateRecord(FollowRecord record);

    /**
     * 删除随访记录
     */
    boolean deleteRecord(Long id);
}
