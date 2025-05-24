package com.example.job_tracker.service.user;

import com.example.job_tracker.dto.UserDto;
import com.example.job_tracker.enums.UserStatus;
import com.example.job_tracker.exception.ResourceNotFoundException;
import com.example.job_tracker.model.User;
import com.example.job_tracker.repository.UserRepository;
import com.example.job_tracker.request.CreateUserRequest;
import com.example.job_tracker.request.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements iUserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserDto createUser(CreateUserRequest req){
        return convertToDto(addUser(req));
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if(req.getLastName() != null) user.setLastName(req.getLastName());
        if(req.getStatus() != null) user.setStatus(req.getStatus());

        return convertToDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    // --- Private helper methods ---
    private User addUser(CreateUserRequest req){
        if(req.getFirstName() == null || req.getLastName() == null || req.getEmail() == null || req.getPassword() == null) {
            throw new IllegalArgumentException("All fields are required");
        }
        User user = new User();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());
        user.setStatus(UserStatus.UNEMPLOYED);
        return userRepository.save(user);
    }

    private UserDto convertToDto(User user) {
        UserDto userDto = modelMapper.map(user, UserDto.class);
        userDto.setPassword(null); // Never expose password
        return userDto;
    }
}
