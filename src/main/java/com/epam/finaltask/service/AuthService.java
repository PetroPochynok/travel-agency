package com.epam.finaltask.service;

import com.epam.finaltask.dto.LoginRequest;
import com.epam.finaltask.dto.RegisterRequest;

import java.util.Map;

public interface AuthService {
    Map<String, Object> login(LoginRequest request);
    void register(RegisterRequest request);
}
