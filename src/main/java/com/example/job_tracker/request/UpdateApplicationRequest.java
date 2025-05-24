package com.example.job_tracker.request;

import com.example.job_tracker.enums.JobStatus;
import lombok.Data;

@Data
public class UpdateApplicationRequest {
    private String jobTitle;
    private JobStatus jobStatus;
    private String companyName;
    private String companyDescription;
    private String description;
}
