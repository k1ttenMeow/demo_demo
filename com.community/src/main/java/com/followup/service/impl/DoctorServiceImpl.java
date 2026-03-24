package com.followup.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.Doctor;
import com.followup.mapper.DoctorMapper;
import com.followup.service.DoctorService;
import org.springframework.stereotype.Service;

/**
 * 医生表 业务实现类
 *
 * @author system
 * @date 2026-03-23
 */
@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

}