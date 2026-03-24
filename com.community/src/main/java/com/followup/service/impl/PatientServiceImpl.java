package com.followup.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.Patient;
import com.followup.mapper.PatientMapper;
import com.followup.service.PatientService;
import org.springframework.stereotype.Service;

/**
 * 患者表 业务实现类
 *
 * @author system
 * @date 2026-03-23
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

}