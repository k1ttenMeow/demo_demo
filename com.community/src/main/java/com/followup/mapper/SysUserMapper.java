package com.followup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.followup.entity.SysUser;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT COUNT(*) FROM user")
    Integer totalUser();

    @Select("SELECT COUNT(*) FROM user WHERE role = 2")
    Integer doctorCount();

    @Select("SELECT COUNT(*) FROM user WHERE role = 3")
    Integer patientCount();

    @Select("SELECT * FROM user ORDER BY create_time DESC LIMIT 10")
    List<SysUser> recentUser();
}