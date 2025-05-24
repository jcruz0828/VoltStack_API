package com.example.job_tracker.controller;

import com.example.job_tracker.dto.ApplicationDto;
import com.example.job_tracker.enums.JobStatus;
import com.example.job_tracker.exception.ResourceNotFoundException;
import com.example.job_tracker.request.CreateApplicationRequest;
import com.example.job_tracker.request.UpdateApplicationRequest;
import com.example.job_tracker.response.ApiResponse;
import com.example.job_tracker.service.application.iApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("${api.base.path}/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final iApplicationService applicationService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> createApplication(@RequestBody CreateApplicationRequest request) {
        try {
            ApplicationDto created = applicationService.createApplication(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(created, "Application created"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(null, "Internal error: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getApplicationById(@PathVariable Long id) {
        try {
            ApplicationDto app = applicationService.getApplicationById(id);
            return ResponseEntity.ok(new ApiResponse(app, "Application found"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(null, e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getByUserId(@PathVariable Long userId) {
        List<ApplicationDto> apps = applicationService.getApplicationsByUserId(userId);
        return ResponseEntity.ok(new ApiResponse(apps, "Applications by user"));
    }

    @GetMapping("/user/{userId}/company/{companyId}")
    public ResponseEntity<ApiResponse> getByUserAndCompanyId(@PathVariable Long userId, @PathVariable Long companyId) {
        List<ApplicationDto> apps = applicationService.getApplicationsByUserIdAndCompanyId(userId, companyId);
        return ResponseEntity.ok(new ApiResponse(apps, "User applications by company ID"));
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<ApiResponse> getByUserAndStatus(@PathVariable Long userId, @PathVariable JobStatus status) {
        List<ApplicationDto> apps = applicationService.getApplicationsByUserIdAndJobStatus(userId, status);
        return ResponseEntity.ok(new ApiResponse(apps, "User applications by status"));
    }

    @GetMapping("/user/{userId}/job-title")
    public ResponseEntity<ApiResponse> getByUserAndTitle(@PathVariable Long userId, @RequestParam String title) {
        List<ApplicationDto> apps = applicationService.getApplicationsByUserIdAndJobTitle(userId, title);
        return ResponseEntity.ok(new ApiResponse(apps, "User applications by job title"));
    }

    @GetMapping("/user/{userId}/company-name")
    public ResponseEntity<ApiResponse> getByUserAndCompanyName(@PathVariable Long userId, @RequestParam String name) {
        List<ApplicationDto> apps = applicationService.getApplicationsByUserIdAndCompanyName(userId, name);
        return ResponseEntity.ok(new ApiResponse(apps, "User applications by company name"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateApplication(@PathVariable Long id, @RequestBody UpdateApplicationRequest request) {
        try {
            ApplicationDto updated = applicationService.updateApplication(id, request);
            return ResponseEntity.ok(new ApiResponse(updated, "Application updated"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(null, e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteApplication(@PathVariable Long id) {
        try {
            applicationService.deleteApplication(id);
            return ResponseEntity.ok(new ApiResponse(null, "Application deleted"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(null, "Internal error: " + e.getMessage()));
        }
    }
}
