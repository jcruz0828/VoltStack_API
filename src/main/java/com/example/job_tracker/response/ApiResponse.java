package com.example.job_tracker.response;

import lombok.Data;

@Data

public class ApiResponse {
    Object data;
    String message;
    public ApiResponse(Object data, String message) {
        this.data = data;
        this.message = message;

    }
}
