// iApplicationService.java
package com.example.job_tracker.service.application;

import com.example.job_tracker.dto.ApplicationDto;
import com.example.job_tracker.enums.JobStatus;
import com.example.job_tracker.request.CreateApplicationRequest;
import com.example.job_tracker.request.UpdateApplicationRequest;

import java.util.List;

public interface iApplicationService {
    ApplicationDto createApplication(CreateApplicationRequest request);
    ApplicationDto getApplicationById(Long id);
    List<ApplicationDto> getAllApplications();
    List<ApplicationDto> getApplicationsByUserId(Long userId);
    List<ApplicationDto> getApplicationsByUserIdAndCompanyId(Long userId, Long companyId);
    List<ApplicationDto> getApplicationsByUserIdAndJobStatus(Long userId, JobStatus status);
    List<ApplicationDto> getApplicationsByUserIdAndJobTitle(Long userId, String jobTitle);
    List<ApplicationDto> getApplicationsByUserIdAndCompanyName(Long userId, String companyName);
    ApplicationDto updateApplication(Long id, UpdateApplicationRequest request);
    void deleteApplication(Long id);
}