package com.example.job_tracker.controller;

import com.example.job_tracker.dto.UserDto;
import com.example.job_tracker.exception.ResourceNotFoundException;
import com.example.job_tracker.request.CreateUserRequest;
import com.example.job_tracker.request.UpdateUserRequest;
import com.example.job_tracker.response.ApiResponse;
import com.example.job_tracker.service.user.iUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("${api.base.path}/users")
@RequiredArgsConstructor
public class UserController {
    private final iUserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id) {
        try {
            UserDto user = userService.getUserById(id);
            return ResponseEntity.ok(new ApiResponse(user, "User retrieved successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(null, "User with id: " + id + " not found"));
        }
        catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(null, "An error occurred while retrieving the user"));
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest req) {
        try {
            UserDto user = userService.updateUser(id, req);
            return ResponseEntity.ok(new ApiResponse(user, "User updated successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(null, "User with id: " + id + " not found"));
        }
        catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(null, "An error occurred while updating the user"));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse(null, "User deleted successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(null, "User with id: " + id + " not found"));
        }
        catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(null, "An error occurred while deleting the user"));
        }
    }
}
