package com.example.job_tracker.service.application;

import com.example.job_tracker.dto.ApplicationDto;
import com.example.job_tracker.enums.JobStatus;
import com.example.job_tracker.exception.ResourceNotFoundException;
import com.example.job_tracker.model.Application;
import com.example.job_tracker.model.Company;
import com.example.job_tracker.model.User;
import com.example.job_tracker.repository.ApplicationRepository;
import com.example.job_tracker.repository.CompanyRepository;
import com.example.job_tracker.repository.UserRepository;
import com.example.job_tracker.request.CreateApplicationRequest;
import com.example.job_tracker.request.UpdateApplicationRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService implements iApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApplicationDto createApplication(CreateApplicationRequest request) {
        Company company = companyRepository.findByNameIgnoreCase(request.getName());

        if (company == null) {
            company = new Company();
            company.setName(request.getName());
            company = companyRepository.save(company);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Application application = new Application();
        application.setJobTitle(request.getJobTitle());
        application.setJobStatus(request.getJobStatus());
        application.setCompany(company);
        application.setUser(user);
        application.setCompanyDescription(request.getCompanyDescription());
        application.setDescription(request.getDescription());// unique per app

        Application saved = applicationRepository.save(application);
        return mapToDto(saved);
    }

    @Override
    public ApplicationDto getApplicationById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        return mapToDto(application);
    }

    @Override
    public List<ApplicationDto> getAllApplications() {
        return applicationRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationDto> getApplicationsByUserId(Long userId) {
        return applicationRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationDto> getApplicationsByUserIdAndCompanyId(Long userId, Long companyId) {
        return applicationRepository.findByUserIdAndCompanyId(userId, companyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationDto> getApplicationsByUserIdAndJobStatus(Long userId, JobStatus status) {
        return applicationRepository.findByUserIdAndJobStatus(userId, status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationDto> getApplicationsByUserIdAndJobTitle(Long userId, String jobTitle) {
        return applicationRepository.findByUserIdAndJobTitleContainingIgnoreCase(userId, jobTitle).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationDto> getApplicationsByUserIdAndCompanyName(Long userId, String companyName) {
        return applicationRepository.findByUserIdAndCompanyNameContainingIgnoreCase(userId, companyName).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ApplicationDto updateApplication(Long id, UpdateApplicationRequest request) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        application.setJobTitle(request.getJobTitle());
        application.setJobStatus(request.getJobStatus());

        String companyName = request.getCompanyName();
        if (companyName != null && !companyName.trim().isEmpty()) {
            Company company = companyRepository.findByNameIgnoreCase(companyName);
            if (company == null) {
                company = new Company();
                company.setName(companyName);
                company = companyRepository.save(company);
            }
            application.setCompany(company);
        }
        // else: do not change the company if name is missing

        application.setCompanyDescription(request.getCompanyDescription());
        application.setDescription(request.getDescription());

        Application updated = applicationRepository.save(application);
        return mapToDto(updated);
    }
    @Override
    public void deleteApplication(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Application not found with id: " + id);
        }
        applicationRepository.deleteById(id);
    }

    private ApplicationDto mapToDto(Application application) {
        ApplicationDto dto = new ApplicationDto();
        dto.setId(application.getId());
        dto.setJobTitle(application.getJobTitle());
        dto.setJobStatus(application.getJobStatus());
        dto.setUserId(application.getUser().getId());
        dto.setCompanyId(application.getCompany().getId());
        dto.setCompanyName(application.getCompany().getName());
        dto.setDescription(application.getDescription()); // Application's own description
        dto.setCompanyDescription(application.getCompany().getDescription()); // Company's description
        return dto;
    }
}
