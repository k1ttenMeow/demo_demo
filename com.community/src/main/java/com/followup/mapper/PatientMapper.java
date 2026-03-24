package com.followup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.followup.entity.Patient;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者表 数据访问层
 *
 * @author system
 * @date 2026-03-23
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {

}