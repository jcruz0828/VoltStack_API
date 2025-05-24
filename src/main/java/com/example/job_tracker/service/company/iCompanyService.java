package com.example.job_tracker.service.company;

import com.example.job_tracker.dto.CompanyDto;
import com.example.job_tracker.request.CreateCompanyRequest;
import com.example.job_tracker.request.UpdateCompanyRequest;

import java.util.List;

public interface iCompanyService {
    CompanyDto createCompany(CreateCompanyRequest req);
    List<CompanyDto> getAllCompanies();
    CompanyDto getCompanyById(Long id);
    CompanyDto findByCompanyName(String companyName);
    CompanyDto updateCompany(Long id, UpdateCompanyRequest req);
    void deleteCompany(Long id);
    List<CompanyDto> findCompaniesByPrefix(String prefix);

}
