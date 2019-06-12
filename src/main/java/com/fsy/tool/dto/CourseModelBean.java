package com.fsy.tool.dto;


import lombok.Getter;
import lombok.Setter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseModelBean {
    private String code;

    private String name;

    private String score;

    private String limitTime;

    private String status;


}
