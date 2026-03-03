package com.floodrescue.module.auth.service;

import com.floodrescue.module.auth.dto.request.*;
import com.floodrescue.module.auth.dto.response.*;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(RefreshTokenRequest request);
    void logout(RefreshTokenRequest request);
    UserResponse getMe(Long userId);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
}