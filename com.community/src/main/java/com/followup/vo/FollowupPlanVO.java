package com.followup.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowupPlanVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String planType;
    private String cycleType;
    private LocalDateTime nextFollowTime;
    private Integer status;
}