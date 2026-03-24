package com.followup.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.Admin;
import com.followup.mapper.AdminMapper;
import com.followup.service.AdminService;
import org.springframework.stereotype.Service;

/**
 * 管理员表 业务实现类
 *
 * @author system
 * @date 2026-03-23
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

}