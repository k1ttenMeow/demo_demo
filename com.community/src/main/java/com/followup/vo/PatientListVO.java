package com.followup.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String realName;
    private Integer gender;
    private Integer age;
    private String chronicDisease;
    private String address;
}