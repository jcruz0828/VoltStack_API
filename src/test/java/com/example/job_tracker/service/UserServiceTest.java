package com.example.job_tracker.service;

import com.example.job_tracker.dto.UserDto;
import com.example.job_tracker.enums.UserStatus;
import com.example.job_tracker.exception.ResourceNotFoundException;
import com.example.job_tracker.model.User;
import com.example.job_tracker.repository.UserRepository;
import com.example.job_tracker.request.CreateUserRequest;
import com.example.job_tracker.request.UpdateUserRequest;
import com.example.job_tracker.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setPassword("secret");
        user.setStatus(UserStatus.UNEMPLOYED);

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setEmail("john@example.com");
        userDto.setStatus(UserStatus.UNEMPLOYED);
    }

    @Test
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = userService.getUserById(1L);
        assertEquals("John", result.getFirstName());
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void createUser_success() {
        CreateUserRequest req = new CreateUserRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@example.com");
        req.setPassword("secret");

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = userService.createUser(req);
        assertEquals("John", result.getFirstName());
    }

    @Test
    void createUser_missingFields_throwsException() {
        CreateUserRequest req = new CreateUserRequest();
        req.setFirstName(null);
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(req));
    }

    @Test
    void updateUser_success() {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setFirstName("Jane");

        user.setFirstName("Jane");
        userDto.setFirstName("Jane");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = userService.updateUser(1L, req);
        assertEquals("Jane", result.getFirstName());
    }

    @Test
    void deleteUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
    }
}
