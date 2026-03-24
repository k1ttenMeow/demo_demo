package com.followup.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String realName;
    private Integer gender;
    private Integer age;
    private String idCard;
    private String chronicDisease;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
}