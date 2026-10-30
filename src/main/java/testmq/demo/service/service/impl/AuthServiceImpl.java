package testmq.demo.service.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import testmq.demo.common.constant.JwtConstants;
import testmq.demo.common.exception.BadCredentialsException;
import testmq.demo.common.exception.IdInvalidException;
import testmq.demo.common.security.jwt.TokenProvider;
import testmq.demo.common.security.service.CustomUserDetail;
import testmq.demo.dto.LoginResult;
import testmq.demo.dto.request.auth.LoginRequestDTO;
import testmq.demo.dto.response.auth.LoginResponseDTO;
import testmq.demo.entity.RefreshToken;
import testmq.demo.entity.User;
import testmq.demo.repository.RefreshTokenRepository;
import testmq.demo.repository.UserRepository;
import testmq.demo.service.service.AuthService;
import testmq.demo.service.service.UserService;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

    final AuthenticationManager authenticationManager;
    final TokenProvider tokenProvider;
    final UserRepository userRepository;
    final RefreshTokenRepository refreshTokenRepository;
    final UserService userService;

    @Value("${app.jwt.refresh-token-validity-in-seconds:2592000}")
    Long refreshTokenExpiration;

    @Transactional
    public LoginResult login(LoginRequestDTO request, String deviceId) {
        String finalDeviceId = (deviceId != null && !deviceId.isBlank()) ? deviceId : "unknown-device";

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetail customUserDetail = (CustomUserDetail) authentication.getPrincipal();
        User user = customUserDetail.getUser();
        UUID userId = user.getUserId();

        String accessToken = this.tokenProvider.createAccessToken(authentication, finalDeviceId);
        String refreshToken = this.tokenProvider.createRefreshToken(authentication, finalDeviceId);

        RefreshToken tokenEntity = this.refreshTokenRepository.findByUserUserIdAndDeviceId(userId, finalDeviceId)
                .orElse(new RefreshToken());
        tokenEntity.setToken(refreshToken);
        tokenEntity.setExpiryDate(Instant.now().plusSeconds(refreshTokenExpiration));
        tokenEntity.setDeviceId(finalDeviceId);
        tokenEntity.setRevoked(false);
        tokenEntity.setUser(user);
        this.refreshTokenRepository.save(tokenEntity);

        log.debug("User {} logged in successfully with deviceId {}", user.getUsername(), finalDeviceId);
        return buildLoginResult(user, accessToken, refreshToken);
    }

    @Transactional
    public LoginResult getNewToken(String refreshToken, String accessToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadCredentialsException("Refresh token is missing");
        }

        Jwt decodeToken;
        try {
            decodeToken = this.tokenProvider.checkValidRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        UUID userId = UUID.fromString(decodeToken.getSubject());
        String deviceId = decodeToken.getClaimAsString(JwtConstants.Claims.DEVICE_ID);
        if (deviceId == null) {
            deviceId = "unknown-device";
        }

        User user = this.userRepository.findByUserId(userId)
                .orElseThrow(() -> new IdInvalidException("User not found"));

        RefreshToken refreshTokenEntity = this.refreshTokenRepository.findByUserUserIdAndDeviceId(userId, deviceId)
                .orElseThrow(() -> new BadCredentialsException("Refresh token session not found"));

        if (Boolean.TRUE.equals(refreshTokenEntity.getRevoked())) {
            log.debug("Refresh token is revoked for userId: {}", userId);
            throw new BadCredentialsException("Refresh token is revoked");
        }

        if (refreshTokenEntity.getExpiryDate().isBefore(Instant.now())) {
            log.debug("Refresh token in DB has expired for userId: {}", userId);
            this.refreshTokenRepository.delete(refreshTokenEntity);
            throw new BadCredentialsException("Refresh token has expired");
        }

        CustomUserDetail userDetail = new CustomUserDetail(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());

        String newAccessToken = this.tokenProvider.createAccessToken(authentication, deviceId);
        String newRefreshToken = this.tokenProvider.createRefreshToken(authentication, deviceId);

        refreshTokenEntity.setToken(newRefreshToken);
        refreshTokenEntity.setExpiryDate(Instant.now().plusSeconds(refreshTokenExpiration));
        this.refreshTokenRepository.save(refreshTokenEntity);

        log.debug("Issued new access token and refresh token for userId: {}", userId);
        return buildLoginResult(user, newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken, String accessToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                Jwt decodeRefreshToken = this.tokenProvider.checkValidRefreshToken(refreshToken);
                UUID userId = UUID.fromString(decodeRefreshToken.getSubject());
                String deviceId = decodeRefreshToken.getClaimAsString(JwtConstants.Claims.DEVICE_ID);

                if (deviceId != null) {
                    this.refreshTokenRepository.findByUserUserIdAndDeviceId(userId, deviceId)
                            .ifPresent(tokenEntity -> {
                                tokenEntity.setRevoked(true);
                                this.refreshTokenRepository.save(tokenEntity);
                            });
                }
            } catch (Exception e) {
                log.debug("Logout token verification skipped: {}", e.getMessage());
            }
        }
        SecurityContextHolder.clearContext();
    }

    public LoginResponseDTO toResponse(User user, String accessToken) {
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .user(this.userService.toResponse(user))
                .build();
    }

    private LoginResult buildLoginResult(User user, String accessToken, String refreshToken) {
        return new LoginResult(toResponse(user, accessToken), refreshToken);
    }
}
