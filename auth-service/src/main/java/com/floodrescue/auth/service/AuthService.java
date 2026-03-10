package com.floodrescue.auth.service;

import com.floodrescue.auth.dtos.request.*;
import com.floodrescue.auth.dtos.response.*;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(RefreshTokenRequest request);
    void logout(RefreshTokenRequest request);
    UserResponse getMe(Long userId);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
}
