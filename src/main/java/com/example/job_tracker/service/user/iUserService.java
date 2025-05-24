package com.example.job_tracker.service.user;

import com.example.job_tracker.dto.UserDto;
import com.example.job_tracker.request.CreateUserRequest;
import com.example.job_tracker.request.UpdateUserRequest;

public interface iUserService {
    UserDto createUser(CreateUserRequest req);
    UserDto getUserById(Long id);
    UserDto updateUser(Long id, UpdateUserRequest req);
    void deleteUser(Long id);
}