package com.followup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.followup.entity.FollowupRecord;
import org.apache.ibatis.annotations.Select;

public interface FollowupRecordMapper extends BaseMapper<FollowupRecord> {

    @Select("SELECT COUNT(*) FROM follow_record")
    Integer followCount();
}