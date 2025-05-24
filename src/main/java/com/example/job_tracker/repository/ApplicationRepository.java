// ApplicationRepository.java
package com.example.job_tracker.repository;

import com.example.job_tracker.enums.JobStatus;
import com.example.job_tracker.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUserId(Long userId);
    List<Application> findByUserIdAndCompanyId(Long userId, Long companyId);
    List<Application> findByUserIdAndJobStatus(Long userId, JobStatus status);
    List<Application> findByUserIdAndJobTitleContainingIgnoreCase(Long userId, String jobTitle);
    List<Application> findByUserIdAndCompanyNameContainingIgnoreCase(Long userId, String companyName);
}
