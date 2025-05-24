package com.example.job_tracker.repository;

import com.example.job_tracker.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    // Custom query methods can be defined here if needed
    // For example, findByCompanyName(String companyName);
    Company findByNameIgnoreCase(String company);

    @Query("SELECT c FROM Company c WHERE LOWER(c.name) LIKE LOWER(CONCAT(:prefix, '%'))")
    List<Company> findByNameStartingWithIgnoreCaseCustom(@Param("prefix") String prefix);




}
