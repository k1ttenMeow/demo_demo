package com.followup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.followup.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生表 数据访问层
 *
 * @author system
 * @date 2026-03-23
 */
@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

}