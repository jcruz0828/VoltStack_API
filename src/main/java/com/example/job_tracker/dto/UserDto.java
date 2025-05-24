package com.example.job_tracker.dto;

import com.example.job_tracker.enums.UserStatus;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private UserStatus status;
}
