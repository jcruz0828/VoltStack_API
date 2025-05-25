package com.example.job_tracker.repository;

import com.example.job_tracker.model.EmailMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailMetadataRepository extends JpaRepository<EmailMetadata, Long> {
}
