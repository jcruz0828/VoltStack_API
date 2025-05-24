package com.example.job_tracker.controller;

import com.example.job_tracker.jwt.JwtUtil;
import com.example.job_tracker.repository.UserRepository;
import com.example.job_tracker.request.LoginRequest;
import com.example.job_tracker.request.RegisterRequest;
import com.example.job_tracker.response.ApiResponse;
import com.example.job_tracker.response.AuthResponse;
import com.example.job_tracker.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.base.path}/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest){
        if (registerRequest.getFirstName() == null || registerRequest.getLastName() == null || registerRequest.getEmail() == null || registerRequest.getPassword() == null) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, "All fields are required"));
        }
        try {
            String token = authService.register(registerRequest);
            return ResponseEntity.ok(new AuthResponse(
                    jwtUtil.extractId(token),
                    token,
                    jwtUtil.extractEmail(token)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new AuthResponse(null, null, "An error occurred while processing your request"));
        }
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest){
        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, "All fields are required"));
        }
        try {
            String token = authService.login(loginRequest);
            return ResponseEntity.ok(new AuthResponse(
                    jwtUtil.extractId(token),
                    token,
                    jwtUtil.extractEmail(token)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new AuthResponse(null, null, "An error occurred while processing your request"));
        }
    }

}
