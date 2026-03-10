package com.floodrescue.auth.service.impl;

import com.floodrescue.auth.dtos.request.*;
import com.floodrescue.auth.dtos.response.LoginResponse;
import com.floodrescue.auth.dtos.response.UserResponse;
import com.floodrescue.auth.domain.entity.RefreshToken;
import com.floodrescue.auth.domain.entity.User;
import com.floodrescue.auth.enums.RoleType;
import com.floodrescue.auth.enums.UserStatus;
import com.floodrescue.auth.repository.RefreshTokenRepository;
import com.floodrescue.auth.repository.UserRepository;
import com.floodrescue.auth.service.AuthService;
import com.floodrescue.auth.exception.AppException;
import com.floodrescue.auth.exception.ErrorCode;
import com.floodrescue.auth.security.JwtTokenProvider;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.expiration-ms}")
    private Long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshExpirationMs;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (request.getRole() == RoleType.COORDINATOR
                || request.getRole() == RoleType.MANAGER
                || request.getRole() == RoleType.ADMIN) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.DUPLICATE_PHONE);
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        return toUserResponse(user);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        String accessToken  = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = generateRefreshToken(user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .user(toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (token.getRevoked() || token.isExpired()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        String newAccessToken  = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user.getId());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .user(toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (request.getEmail() != null
                && userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new AppException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getEmail()    != null) user.setEmail(request.getEmail());
        if (request.getLat()      != null) user.setLat(request.getLat());
        if (request.getLng()      != null) user.setLng(request.getLng());

        return toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Mật khẩu xác nhận không khớp");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Mật khẩu hiện tại không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        refreshTokenRepository.revokeAllByUserId(userId);
        userRepository.save(user);
    }

    // ==================== PRIVATE ====================

    private String generateRefreshToken(Long userId) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken).getToken();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .lat(user.getLat())
                .lng(user.getLng())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
