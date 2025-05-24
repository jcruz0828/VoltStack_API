package com.example.job_tracker.controller;

import com.example.job_tracker.dto.UserDto;
import com.example.job_tracker.enums.UserStatus;
import com.example.job_tracker.exception.ResourceNotFoundException;
import com.example.job_tracker.request.UpdateUserRequest;
import com.example.job_tracker.service.user.iUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserControllerTest.MockConfig.class)
class UserControllerTest {
    static class MockConfig {
        @Bean
        public iUserService userService() {
            return mock(iUserService.class);
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
    private iUserService userService;



    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setEmail("john@example.com");
        userDto.setPassword(null);
        userDto.setStatus(UserStatus.UNEMPLOYED);
    }

    @Test
    void getUserById_success() throws Exception {
        when(userService.getUserById(eq(1L))).thenReturn(userDto);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }

    @Test
    void getUserById_notFound() throws Exception {
        when(userService.getUserById(1L)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id: 1 not found"));
    }

    @Test
    void updateUser_success() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Jane");
        request.setStatus(UserStatus.EMPLOYED);

        userDto.setFirstName("Jane");
        userDto.setStatus(UserStatus.EMPLOYED);

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(userDto);

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated successfully"))
                .andExpect(jsonPath("$.data.firstName").value("Jane"));
    }

    @Test
    void updateUser_notFound() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Jane");

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id: 1 not found"));
    }

    @Test
    void deleteUser_success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    void deleteUser_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("User not found")).when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id: 1 not found"));
    }
}
