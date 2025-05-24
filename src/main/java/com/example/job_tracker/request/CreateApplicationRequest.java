package com.example.job_tracker.request;

import com.example.job_tracker.enums.JobStatus;
import lombok.Data;

@Data
public class CreateApplicationRequest {
    private String jobTitle;
    private JobStatus jobStatus;
    private String name;
    private String description;
    private String companyDescription;
    private Long userId;
}
