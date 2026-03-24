package com.followup.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatsVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer patientCount;     // 我的患者
    private Integer pendingCount;     // 待办随访
    private Integer completedCount;   // 本月完成
    private Integer appointmentCount; // 待确认预约
}