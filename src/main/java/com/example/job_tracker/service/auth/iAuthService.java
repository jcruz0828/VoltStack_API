package com.example.job_tracker.service.auth;

import com.example.job_tracker.request.LoginRequest;
import com.example.job_tracker.request.RegisterRequest;


public interface iAuthService {
    public String register(RegisterRequest req);
    public String login(LoginRequest req);

}
