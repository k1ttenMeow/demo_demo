package com.followup.service;

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
