package com.example.job_tracker.controller;

import com.example.job_tracker.dto.CompanyDto;
import com.example.job_tracker.exception.ResourceNotFoundException;
import com.example.job_tracker.request.CreateCompanyRequest;
import com.example.job_tracker.request.UpdateCompanyRequest;
import com.example.job_tracker.service.company.iCompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CompanyControllerTest.MockConfig.class)
public class CompanyControllerTest {

    static class MockConfig {
        @Bean
        public iCompanyService companyService() {
            return mock(iCompanyService.class);
        }

        @Bean
        public com.example.job_tracker.jwt.JwtUtil jwtUtil() {
            return mock(com.example.job_tracker.jwt.JwtUtil.class);
        }

        @Bean
        public com.example.job_tracker.service.auth.CustomUserDetailsService customUserDetailsService() {
            return mock(com.example.job_tracker.service.auth.CustomUserDetailsService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private iCompanyService companyService;

    private CompanyDto companyDto;

    @BeforeEach
    void setUp() {
        companyDto = new CompanyDto();
        companyDto.setId(1L);
        companyDto.setName("Test Company");
        companyDto.setDescription("A test company for unit testing");
    }

    @Test
    void createCompanyTest() throws Exception {
        when(companyService.createCompany(any())).thenReturn(companyDto);

        CreateCompanyRequest request = new CreateCompanyRequest();
        request.setName("Test Company");
        request.setDescription("A test company for unit testing");

        mockMvc.perform(post("/api/v1/companies")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test Company"))
                .andExpect(jsonPath("$.data.description").value("A test company for unit testing"));
    }

    @Test
    void createCompanyTest_Failure() throws Exception {
        when(companyService.createCompany(any())).thenThrow(new RuntimeException("Failed to create company"));

        CreateCompanyRequest request = new CreateCompanyRequest();
        request.setName("Test Company");
        request.setDescription("A test company for unit testing");

        mockMvc.perform(post("/api/v1/companies")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to create company"));
    }

    @Test
    void getAllCompaniesTest() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(List.of(companyDto));

        mockMvc.perform(get("/api/v1/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Test Company"))
                .andExpect(jsonPath("$.data[0].description").value("A test company for unit testing"));
    }

    @Test
    void getAllCompaniesTest_Failure() throws Exception {
        when(companyService.getAllCompanies()).thenThrow(new RuntimeException("Failed to fetch companies"));

        mockMvc.perform(get("/api/v1/companies"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to fetch companies"));
    }

    @Test
    void getCompanyByIdTest() throws Exception {
        when(companyService.getCompanyById(eq(1L))).thenReturn(companyDto);

        mockMvc.perform(get("/api/v1/companies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test Company"))
                .andExpect(jsonPath("$.data.description").value("A test company for unit testing"));
    }

    @Test
    void getCompanyByIdTest_NotFound() throws Exception {
        when(companyService.getCompanyById(1L)).thenThrow(new ResourceNotFoundException("Company not found"));

        mockMvc.perform(get("/api/v1/companies/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company with id: 1 not found"));
    }

    @Test
    void getCompanyByNameTest() throws Exception {
        when(companyService.findByCompanyName("Test Company")).thenReturn(companyDto);

        mockMvc.perform(get("/api/v1/companies/search").param("name", "Test Company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test Company"))
                .andExpect(jsonPath("$.data.description").value("A test company for unit testing"));
    }

    @Test
    void getCompanyByNameTest_NotFound() throws Exception {
        when(companyService.findByCompanyName("Nonexistent Company"))
                .thenThrow(new ResourceNotFoundException("Company not found"));

        mockMvc.perform(get("/api/v1/companies/search").param("name", "Nonexistent Company"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company with name: Nonexistent Company not found"));
    }

    @Test
    void updateCompanyTest() throws Exception {
        UpdateCompanyRequest request = new UpdateCompanyRequest();
        request.setCompany("Updated Company");
        request.setDescription("An updated test company for unit testing");

        companyDto.setName("Updated Company");
        companyDto.setDescription("An updated test company for unit testing");

        when(companyService.updateCompany(eq(1L), any())).thenReturn(companyDto);

        mockMvc.perform(put("/api/v1/companies/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Company"))
                .andExpect(jsonPath("$.data.description").value("An updated test company for unit testing"));
    }

    @Test
    void updateCompanyTest_NotFound() throws Exception {
        when(companyService.updateCompany(eq(1L), any()))
                .thenThrow(new ResourceNotFoundException("Company not found"));

        CreateCompanyRequest request = new CreateCompanyRequest();
        request.setName("Updated Company");
        request.setDescription("An updated test company for unit testing");

        mockMvc.perform(put("/api/v1/companies/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company with id: 1 not found"));
    }

    @Test
    void deleteCompanyTest() throws Exception {
        doNothing().when(companyService).deleteCompany(1L);

        mockMvc.perform(delete("/api/v1/companies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Company deleted successfully"));
    }

    @Test
    void deleteCompanyTest_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Company not found")).when(companyService).deleteCompany(1L);

        mockMvc.perform(delete("/api/v1/companies/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company with id: 1 not found"));
    }

    @Test
    void deleteCompanyTest_InternalError() throws Exception {
        doThrow(new RuntimeException("An error occurred while deleting the company")).when(companyService).deleteCompany(1L);

        mockMvc.perform(delete("/api/v1/companies/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An error occurred while deleting the company"));
    }

    @Test
    void getCompaniesByPrefixTest() throws Exception {
        when(companyService.findCompaniesByPrefix("Test")).thenReturn(List.of(companyDto));

        mockMvc.perform(get("/api/v1/companies/autocomplete").param("prefix", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Test Company"))
                .andExpect(jsonPath("$.data[0].description").value("A test company for unit testing"));
    }
}
