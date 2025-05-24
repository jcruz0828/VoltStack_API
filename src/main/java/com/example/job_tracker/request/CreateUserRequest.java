package com.example.job_tracker.request;


import com.example.job_tracker.enums.UserStatus;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private UserStatus status;
}
