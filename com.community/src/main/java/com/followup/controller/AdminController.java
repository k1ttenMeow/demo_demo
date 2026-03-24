package com.followup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.entity.Admin;
import com.followup.service.AdminService;
import com.followup.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 管理员模块 控制器
 *
 * @author system
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @PostMapping("/add")
    public ResultVO<Boolean> addAdmin(@RequestBody Admin admin) {
        return ResultVO.success(adminService.save(admin));
    }

    @DeleteMapping("/delete/{id}")
    public ResultVO<Boolean> deleteAdmin(@PathVariable Long id) {
        return ResultVO.success(adminService.removeById(id));
    }

    @PutMapping("/update")
    public ResultVO<Boolean> updateAdmin(@RequestBody Admin admin) {
        return ResultVO.success(adminService.updateById(admin));
    }

    @GetMapping("/info/{id}")
    public ResultVO<Admin> getAdminInfo(@PathVariable Long id) {
        return ResultVO.success(adminService.getById(id));
    }

    @GetMapping("/page")
    public ResultVO<Page<Admin>> getAdminPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Admin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Admin::getCreateTime);
        return ResultVO.success(adminService.page(page, wrapper));
    }
}