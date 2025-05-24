package com.example.job_tracker.dto;

import com.example.job_tracker.enums.JobStatus;
import lombok.Data;

@Data
public class ApplicationDto {
    private Long id;
    private String jobTitle;
    private JobStatus jobStatus;
    private Long userId;
    private Long companyId;
    private String companyName;
    private String companyDescription;
    private String description;
}
