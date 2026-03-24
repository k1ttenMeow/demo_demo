package com.followup.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer totalUsers;      // 系统总用户
    private Integer doctorCount;     // 注册医生
    private Integer patientCount;    // 在管患者
    private Integer followupCount;   // 随访记录
}