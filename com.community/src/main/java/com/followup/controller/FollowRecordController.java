package com.followup.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.common.R;
import com.followup.entity.FollowRecord;
import com.followup.service.FollowRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;

@RestController
@RequestMapping("/followup/record")
public class FollowRecordController {

    private static final Logger log = LoggerFactory.getLogger(FollowRecordController.class);

    @Resource
    private FollowRecordService followRecordService;

    /**
     * 分页查询随访记录列表
     */
    @GetMapping("/list")
    public R<Page<FollowRecord>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        try {
            Page<FollowRecord> followPage = followRecordService.getRecordList(
                    page, size, patientName, doctorName,
                    startDate != null ? startDate.toString() : null,
                    endDate != null ? endDate.toString() : null
            );
            return R.success(followPage);
        } catch (Exception e) {
            log.error("查询随访记录列表异常", e);
            return R.error("查询失败");
        }
    }

    /**
     * 创建随访记录
     */
    @PostMapping
    public R<Boolean> create(@RequestBody FollowRecord record) {
        try {
            boolean result = followRecordService.createRecord(record);
            return R.success(result);
        } catch (Exception e) {
            log.error("创建随访记录异常", e);
            return R.error("创建失败");
        }
    }

    /**
     * 更新随访记录
     */
    @PutMapping
    public R<Boolean> update(@RequestBody FollowRecord record) {
        try {
            boolean result = followRecordService.updateRecord(record);
            return R.success(result);
        } catch (Exception e) {
            log.error("更新随访记录异常", e);
            return R.error("更新失败");
        }
    }

    /**
     * 删除随访记录
     */
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        try {
            boolean result = followRecordService.deleteRecord(id);
            return R.success(result);
        } catch (Exception e) {
            log.error("删除随访记录异常", e);
            return R.error("删除失败");
        }
    }
}
