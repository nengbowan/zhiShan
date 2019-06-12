package com.fsy.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionModelBean {
    private String courseId;

    private String courseName;

    private String sectionId;

    private String sectionName;
}
