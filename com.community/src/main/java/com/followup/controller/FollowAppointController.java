package com.followup.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.common.R;
import com.followup.entity.FollowAppoint;
import com.followup.service.FollowAppointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/followup/appoint")
public class FollowAppointController {

    private static final Logger log = LoggerFactory.getLogger(FollowAppointController.class);

    @Resource
    private FollowAppointService followAppointService;

    /**
     * 分页查询随访预约列表
     */
    @GetMapping("/list")
    public R<Page<FollowAppoint>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) String status
    ) {
        try {
            Page<FollowAppoint> followPage = followAppointService.getAppointList(page, size, patientName, doctorName, status);
            return R.success(followPage);
        } catch (Exception e) {
            log.error("查询随访预约列表异常", e);
            return R.error("查询失败");
        }
    }

    /**
     * 创建随访预约
     */
    @PostMapping
    public R<Boolean> create(@RequestBody FollowAppoint appoint) {
        try {
            boolean result = followAppointService.createAppoint(appoint);
            return R.success(result);
        } catch (Exception e) {
            log.error("创建随访预约异常", e);
            return R.error("创建失败");
        }
    }

    /**
     * 更新随访预约
     */
    @PutMapping
    public R<Boolean> update(@RequestBody FollowAppoint appoint) {
        try {
            boolean result = followAppointService.updateAppoint(appoint);
            return R.success(result);
        } catch (Exception e) {
            log.error("更新随访预约异常", e);
            return R.error("更新失败");
        }
    }

    /**
     * 删除随访预约
     */
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        try {
            boolean result = followAppointService.deleteAppoint(id);
            return R.success(result);
        } catch (Exception e) {
            log.error("删除随访预约异常", e);
            return R.error("删除失败");
        }
    }
}
