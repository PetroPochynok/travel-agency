package com.epam.finaltask.dto;

import com.epam.finaltask.annotation.Sensitive;
import lombok.Data;

@Data
public class LoginRequest {
    private String username;

    @Sensitive
    private String password;
}