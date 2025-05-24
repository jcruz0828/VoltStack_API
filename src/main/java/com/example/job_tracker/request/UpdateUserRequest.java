package com.example.job_tracker.request;

import com.example.job_tracker.enums.UserStatus;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private UserStatus status;
}
