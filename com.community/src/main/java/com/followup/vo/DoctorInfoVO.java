package com.followup.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String realName;
    private String deptName;
    private String title;
    private String specialty;
    private String manageCommunity;
    private String appointmentTime;
}