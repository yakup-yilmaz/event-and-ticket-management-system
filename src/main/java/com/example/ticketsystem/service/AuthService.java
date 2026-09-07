package com.example.ticketsystem.service;

import com.example.ticketsystem.dto.AuthResponse;
import com.example.ticketsystem.dto.LoginRequest;
import com.example.ticketsystem.dto.RefreshTokenRequest;
import com.example.ticketsystem.dto.UserCreateRequest;
import com.example.ticketsystem.dto.UserResponse;

public interface AuthService {

    UserResponse register(UserCreateRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(String accessToken, String refreshToken);
}
