package com.example.ticketsystem.service;

import com.example.ticketsystem.dto.UserProfileUpdateRequest;
import com.example.ticketsystem.dto.UserResponse;
import java.util.List;

public interface UserService {

    UserResponse getCurrentUser();

    UserResponse updateCurrentUserProfile(UserProfileUpdateRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();
}
