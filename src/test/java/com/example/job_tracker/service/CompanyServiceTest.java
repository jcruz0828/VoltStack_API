package com.example.job_tracker.service.company;

import com.example.job_tracker.dto.CompanyDto;
import com.example.job_tracker.exception.ResourceNotFoundException;
import com.example.job_tracker.model.Company;
import com.example.job_tracker.repository.CompanyRepository;
import com.example.job_tracker.request.CreateCompanyRequest;
import com.example.job_tracker.request.UpdateCompanyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CompanyService companyService;

    private Company company;
    private CompanyDto companyDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        company = new Company();
        company.setId(1L);
        company.setName("Acme Corp");
        company.setDescription("Description");
        companyDto = new CompanyDto();
        companyDto.setId(1L);
        companyDto.setName("Acme Corp");
        companyDto.setDescription("Description");
    }

    @Test
    void createCompany_success() {
        CreateCompanyRequest req = new CreateCompanyRequest();
        req.setName("Acme Corp");
        req.setDescription("Description");

        when(modelMapper.map(req, Company.class)).thenReturn(company);
        when(companyRepository.save(company)).thenReturn(company);
        when(modelMapper.map(company, CompanyDto.class)).thenReturn(companyDto);

        CompanyDto result = companyService.createCompany(req);
        assertEquals("Acme Corp", result.getName());
    }

    @Test
    void getAllCompanies_success() {
        when(companyRepository.findAll()).thenReturn(Collections.singletonList(company));
        when(modelMapper.map(company, CompanyDto.class)).thenReturn(companyDto);

        List<CompanyDto> result = companyService.getAllCompanies();
        assertEquals(1, result.size());
    }

    @Test
    void getCompanyById_success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(modelMapper.map(company, CompanyDto.class)).thenReturn(companyDto);

        CompanyDto result = companyService.getCompanyById(1L);
        assertEquals("Acme Corp", result.getName());
    }

    @Test
    void getCompanyById_notFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> companyService.getCompanyById(1L));
    }

    @Test
    void findByCompanyName_success() {
        when(companyRepository.findByNameIgnoreCase("Acme Corp")).thenReturn(company);
        when(modelMapper.map(company, CompanyDto.class)).thenReturn(companyDto);

        CompanyDto result = companyService.findByCompanyName("Acme Corp");
        assertEquals("Acme Corp", result.getName());
    }

    @Test
    void findByCompanyName_notFound() {
        when(companyRepository.findByNameIgnoreCase("Unknown")).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> companyService.findByCompanyName("Unknown"));
    }

    @Test
    void updateCompany_success() {
        UpdateCompanyRequest req = new UpdateCompanyRequest();
        req.setCompany("New Name");
        req.setDescription("New Desc");

        Company updatedCompany = new Company();
        updatedCompany.setId(1L);
        updatedCompany.setName("New Name");
        updatedCompany.setDescription("New Desc");
        CompanyDto updatedDto = new CompanyDto();
        updatedDto.setId(1L);
        updatedDto.setName("New Name");
        updatedDto.setDescription("New Desc");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenReturn(updatedCompany);
        when(modelMapper.map(updatedCompany, CompanyDto.class)).thenReturn(updatedDto);

        CompanyDto result = companyService.updateCompany(1L, req);
        assertEquals("New Name", result.getName());
    }

    @Test
    void deleteCompany_success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        doNothing().when(companyRepository).delete(company);

        assertDoesNotThrow(() -> companyService.deleteCompany(1L));
    }

    @Test
    void deleteCompany_notFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> companyService.deleteCompany(1L));
    }

    @Test
    void findCompaniesByPrefix_success() {
        when(companyRepository.findByNameStartingWithIgnoreCaseCustom("Ac"))
                .thenReturn(Collections.singletonList(company));
        when(modelMapper.map(company, CompanyDto.class)).thenReturn(companyDto);

        List<CompanyDto> result = companyService.findCompaniesByPrefix("Ac");
        assertEquals(1, result.size());
    }

    @Test
    void findCompaniesByPrefix_notFound() {
        when(companyRepository.findByNameStartingWithIgnoreCaseCustom("Zz"))
                .thenReturn(Collections.emptyList());
        assertThrows(ResourceNotFoundException.class, () -> companyService.findCompaniesByPrefix("Zz"));
    }
}
