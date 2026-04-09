package com.followup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.followup.common.R;
import com.followup.entity.FollowRecord;
import com.followup.entity.SysUser;
import com.followup.mapper.SysUserMapper;
import com.followup.service.FollowRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/followup/record")
public class FollowRecordController {

    private static final Logger log = LoggerFactory.getLogger(FollowRecordController.class);

    @Resource
    private FollowRecordService followRecordService;

    @Resource
    private SysUserMapper sysUserMapper;

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
     * 获取患者的随访记录列表（患者端专用）
     */
    @GetMapping("/my/{patientId}")
    public R<Page<Map<String, Object>>> getMyRecords(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate followTime
    ) {
        try {
            System.out.println("=== 查询患者随访记录列表 ===");
            System.out.println("patientId (user.id): " + patientId);

            Page<FollowRecord> recordPage = new Page<>(page, size);

            LambdaQueryWrapper<FollowRecord> wrapper = new LambdaQueryWrapper<>();
            // ✅ patient_id 直接对应 user.id
            wrapper.eq(FollowRecord::getPatientId, patientId);

            // 动态添加查询条件
            if (followTime != null) {
                wrapper.eq(FollowRecord::getFollowTime, followTime);
            }

            wrapper.orderByDesc(FollowRecord::getFollowTime);

            Page<FollowRecord> result = followRecordService.page(recordPage, wrapper);

            System.out.println("查询到的原始记录数：" + result.getRecords().size());
            System.out.println("总记录数：" + result.getTotal());

            // 打印每条记录的详细信息
            result.getRecords().forEach(record -> {
                System.out.println("  - 记录ID: " + record.getId() +
                        ", patientId: " + record.getPatientId() +
                        ", doctorId: " + record.getDoctorId() +
                        ", 随访时间: " + record.getFollowTime() +
                        ", 血压: " + record.getBloodPressure() +
                        ", 血糖: " + record.getBloodSugar());
            });

            // 转换为 Map 并填充医生信息
            List<Map<String, Object>> resultList = result.getRecords().stream().map(record -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", record.getId());
                map.put("patientId", record.getPatientId());
                map.put("doctorId", record.getDoctorId());
                map.put("followTime", record.getFollowTime());
                map.put("bloodPressure", record.getBloodPressure());
                map.put("bloodSugar", record.getBloodSugar());
                map.put("drug", record.getDrug());
                map.put("symptom", record.getSymptom());
                map.put("remark", record.getRemark());

                // ✅ doctor_id 直接对应 user.id，直接查询用户表获取医生姓名
                if (record.getDoctorId() != null) {
                    SysUser doctorUser = sysUserMapper.selectById(record.getDoctorId());
                    if (doctorUser != null) {
                        map.put("doctorName", doctorUser.getRealName());
                        System.out.println("  - 医生姓名: " + doctorUser.getRealName());
                    }
                }

                return map;
            }).collect(Collectors.toList());

            Page<Map<String, Object>> resultPage = new Page<>(page, size);
            resultPage.setRecords(resultList);
            resultPage.setTotal(result.getTotal());

            System.out.println("最终返回的记录数量：" + resultList.size());

            return R.success(resultPage);
        } catch (Exception e) {
            log.error("查询患者随访记录列表异常", e);
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
