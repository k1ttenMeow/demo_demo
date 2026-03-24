package com.followup.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowupRecordVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long planId;
    private Long patientId;
    private Long doctorId;
    private String doctorName;
    private LocalDateTime followTime;
    private String bloodPressure;
    private BigDecimal bloodSugar;
    private String medication;
    private String symptoms;
    private String doctorRemark;
}