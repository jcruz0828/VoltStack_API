package com.example.job_tracker.service.auth;

import com.example.job_tracker.jwt.JwtUtil;
import com.example.job_tracker.model.User;
import com.example.job_tracker.repository.UserRepository;
import com.example.job_tracker.request.LoginRequest;
import com.example.job_tracker.request.RegisterRequest;
import com.example.job_tracker.response.AuthResponse;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AuthService implements iAuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;

    @Override
    public String register(RegisterRequest req) {
        if (Stream.of(req.getFirstName(), req.getLastName(), req.getEmail(), req.getPassword())
                .anyMatch(s -> s == null || s.trim().isEmpty())) {
            throw new IllegalArgumentException("All fields are required");
        }

        if (userRepository.findByEmail(req.getEmail().toLowerCase().trim()) != null) {
            throw new IllegalArgumentException("User already exists");
        }

        User newUser = new User();
        newUser.setFirstName(req.getFirstName().trim());
        newUser.setLastName(req.getLastName().trim());
        newUser.setEmail(req.getEmail().toLowerCase().trim());
        newUser.setPassword(passwordEncoder.encode(req.getPassword()));

        User savedUser = userRepository.save(newUser);
        return jwtUtil.generateToken(savedUser.getEmail(), savedUser.getId());
    }


    @Override
    public String login(LoginRequest req) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );
            User user = userRepository.findByEmail(req.getEmail().toLowerCase().trim());
            return jwtUtil.generateToken(req.getEmail(), user.getId());
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }
    }
}
