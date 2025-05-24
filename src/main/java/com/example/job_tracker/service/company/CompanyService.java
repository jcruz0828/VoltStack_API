// CompanyService.java
package com.example.job_tracker.service.company;

import com.example.job_tracker.dto.CompanyDto;
import com.example.job_tracker.exception.ResourceNotFoundException;
import com.example.job_tracker.model.Company;
import com.example.job_tracker.repository.CompanyRepository;
import com.example.job_tracker.request.CreateCompanyRequest;
import com.example.job_tracker.request.UpdateCompanyRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService implements iCompanyService {

    private final CompanyRepository companyRepository;
    private final ModelMapper modelMapper;

    @Override
    public CompanyDto createCompany(CreateCompanyRequest req) {
        Company company = modelMapper.map(req, Company.class);
        Company saved = companyRepository.save(company);
        return modelMapper.map(saved, CompanyDto.class);
    }

    @Override
    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(company -> modelMapper.map(company, CompanyDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public CompanyDto getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        return modelMapper.map(company, CompanyDto.class);
    }

    @Override
    public CompanyDto findByCompanyName(String companyName) {
        Company company = Optional.ofNullable(companyRepository.findByNameIgnoreCase(companyName))
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with name: " + companyName));
        return modelMapper.map(company, CompanyDto.class);
    }

    @Override
    public CompanyDto updateCompany(Long id, UpdateCompanyRequest updateCompanyRequest) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        company.setName(updateCompanyRequest.getCompany());
        company.setDescription(updateCompanyRequest.getDescription());
        Company updated = companyRepository.save(company);
        return modelMapper.map(updated, CompanyDto.class);
    }

    @Override
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        companyRepository.delete(company);
    }
    @Override
    public List<CompanyDto> findCompaniesByPrefix(String prefix) {
        List<Company> companies = companyRepository.findByNameStartingWithIgnoreCaseCustom(prefix);

        if (companies.isEmpty()) {
            throw new ResourceNotFoundException("No companies found starting with: " + prefix);
        }

        return companies.stream()
                .map(company -> modelMapper.map(company, CompanyDto.class))
                .collect(Collectors.toList());
    }

}
