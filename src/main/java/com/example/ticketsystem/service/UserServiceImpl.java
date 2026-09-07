package com.example.ticketsystem.service;

import com.example.ticketsystem.dto.UserProfileUpdateRequest;
import com.example.ticketsystem.dto.UserResponse;
import com.example.ticketsystem.entity.User;
import com.example.ticketsystem.exception.ResourceNotFoundException;
import com.example.ticketsystem.mapper.UserMapper;
import com.example.ticketsystem.repository.UserRepository;
import com.example.ticketsystem.security.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.debug("Mevcut kullanıcı profili. ID: {}", userId);
        return getUserById(userId);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUserProfile(UserProfileUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanici bulunamadi ID: " + userId));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        User saved = userRepository.save(user);

        log.info("Profil güncellendi. User ID: {}", userId);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Kullanıcı detayı. ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanici bulunamadi ID: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Tüm kullanıcılar listeleniyor");
        return userMapper.toResponseList(userRepository.findAll());
    }
}
