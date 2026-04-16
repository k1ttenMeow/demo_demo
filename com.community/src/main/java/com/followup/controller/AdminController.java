package com.followup.controller;

import com.followup.common.R;
import com.followup.entity.SysUser;
import com.followup.service.AdminService;
import com.followup.vo.SysUserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Resource
    private AdminService adminService;

    // 统计接口：异常时返回标准错误 JSON
    @GetMapping("/stats")
    public R<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = adminService.getStats();
            // 终极兜底：确保永远返回非 null 的 Map
            if (stats == null) {
                stats = new HashMap<>();
                stats.put("totalUser", 0);
                stats.put("doctorCount", 0);
                stats.put("patientCount", 0);
                stats.put("followCount", 0);
            }
            return R.success(stats);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return R.error("获取统计数据失败");
        }
    }

    // 新增：分页查询用户列表
    @GetMapping("/user/list")
    public R<Page<SysUserVO>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) String phone
    ) {
        try {
            Page<SysUser> userPage = adminService.getUserList(page, size, username, realName, userType, phone);

            // 转换为 VO 对象，确保 role 字段正确映射
            Page<SysUserVO> voPage = new Page<>(page, size);
            voPage.setTotal(userPage.getTotal());
            voPage.setRecords(userPage.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList()));

            return R.success(voPage);
        } catch (Exception e) {
            log.error("查询用户列表异常", e);
            return R.error("查询失败");
        }
    }

    // 最近用户接口：异常时返回标准错误 JSON
    @GetMapping("/recentUser")
    public R<List<SysUserVO>> getRecentUser() {
        try {
            List<SysUser> users = adminService.getRecentUser();

            // 转换为 VO 对象，确保 role 字段正确映射
            List<SysUserVO> voList = users.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return R.success(voList);
        } catch (Exception e) {
            log.error("获取最近用户失败", e);
            return R.error("获取最近用户失败");
        }
    }

    // 删除用户接口
    @DeleteMapping("/user/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        try {
            return R.success(adminService.deleteUser(id));
        } catch (Exception e) {
            log.error("删除用户异常", e);
            return R.error("删除失败");
        }
    }

    // 新增：注册用户接口
    @PostMapping("/user")
    public R<Boolean> register(@RequestBody SysUser user) {
        try {
            return R.success(adminService.registerUser(user));
        } catch (Exception e) {
            log.error("注册用户异常", e);
            return R.error("注册失败");
        }
    }

    // 更新用户接口
    @PutMapping("/user")
    public R<Boolean> update(@RequestBody SysUser user) {
        try {
            return R.success(adminService.updateUser(user));
        } catch (Exception e) {
            log.error("修改用户异常", e);
            return R.error("修改失败");
        }
    }

    /**
     * 将 SysUser 实体转换为 SysUserVO
     * 确保 role 字段正确映射
     */
    private SysUserVO convertToVO(SysUser user) {
        SysUserVO vo = new SysUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole()); // ✅ 改为 getRole()
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }
}
