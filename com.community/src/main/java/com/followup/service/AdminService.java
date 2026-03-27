package com.followup.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.entity.SysUser;
import java.util.List;
import java.util.Map;

public interface AdminService {
    /**
     * 获取统计数据
     */
    Map<String, Object> getStats();

    /**
     * 最近用户
     */
    List<SysUser> getRecentUser();

    /**
     * 分页查询用户列表
     */
    Page<SysUser> getUserList(Integer page, Integer size, String username, String realName, Integer userType, String phone);

    /**
     * 注册用户
     */
    boolean registerUser(SysUser user);

    /**
     * 删除用户
     */
    boolean deleteUser(Long id);

    /**
     * 修改用户
     */
    boolean updateUser(SysUser user);

}
