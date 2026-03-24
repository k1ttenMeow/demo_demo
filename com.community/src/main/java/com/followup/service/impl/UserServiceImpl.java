package com.followup.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.followup.entity.User;
import com.followup.mapper.UserMapper;
import com.followup.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户表 业务实现类
 *
 * @author system
 * @date 2026-03-23
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}