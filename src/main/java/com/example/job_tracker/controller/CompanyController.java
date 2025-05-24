package com.example.job_tracker.controller;

import com.example.job_tracker.dto.CompanyDto;
import com.example.job_tracker.exception.ResourceNotFoundException;
import com.example.job_tracker.request.CreateCompanyRequest;
import com.example.job_tracker.request.UpdateCompanyRequest;
import com.example.job_tracker.response.ApiResponse;
import com.example.job_tracker.service.company.iCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("${api.base.path}/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final iCompanyService companyService;

    @PostMapping
    public ResponseEntity<ApiResponse> createCompany(@RequestBody CreateCompanyRequest request) {
        try {
            CompanyDto created = companyService.createCompany(request);
            return ResponseEntity.ok(new ApiResponse(created, "Company created successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(null, "Failed to create company"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllCompanies() {
        try {
            List<CompanyDto> companies = companyService.getAllCompanies();
            return ResponseEntity.ok(new ApiResponse(companies, "Companies fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(null, "Failed to fetch companies"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCompanyById(@PathVariable Long id) {
        try {
            CompanyDto company = companyService.getCompanyById(id);
            return ResponseEntity.ok(new ApiResponse(company, "Company retrieved successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(null, "Company with id: " + id + " not found"));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(null, "An error occurred while retrieving the company"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> getCompanyByName(@RequestParam("name") String companyName) {
        try {
            CompanyDto companies = companyService.findByCompanyName(companyName);
            return ResponseEntity.ok(new ApiResponse(companies, "Company retrieved successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(null, "Company with name: " + companyName + " not found"));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(null, "An error occurred while retrieving the company"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateCompany(@PathVariable Long id, @RequestBody UpdateCompanyRequest request) {
        try {
            CompanyDto updated = companyService.updateCompany(id, request);
            return ResponseEntity.ok(new ApiResponse(updated, "Company updated successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(null, "Company with id: " + id + " not found"));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(null, "An error occurred while updating the company"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCompany(@PathVariable Long id) {
        try {
            companyService.deleteCompany(id);
            return ResponseEntity.ok(new ApiResponse(null, "Company deleted successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(null, "Company with id: " + id + " not found"));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(null, "An error occurred while deleting the company"));
        }
    }
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse> getCompaniesByPrefix(@RequestParam("prefix") String prefix) {
        try {
            List<CompanyDto> results = companyService.findCompaniesByPrefix(prefix);
            return ResponseEntity.ok(new ApiResponse(results, "Companies found for prefix: " + prefix));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(null, "Internal error while searching for companies"));
        }
    }

}
