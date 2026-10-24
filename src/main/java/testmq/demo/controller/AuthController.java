package testmq.demo.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import testmq.demo.common.constant.TokenConstants;
import testmq.demo.common.security.SecurityUtils;
import testmq.demo.common.utils.CookieUtils;
import testmq.demo.common.utils.annotation.ApiMessage;
import testmq.demo.dto.LoginResult;
import testmq.demo.dto.request.auth.LoginRequestDTO;
import testmq.demo.dto.request.auth.RegisterRequestDTO;
import testmq.demo.dto.response.auth.LoginResponseDTO;
import testmq.demo.service.AuthService;
import testmq.demo.service.UserService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthController {

    final AuthService authService;
    final UserService userService;

    @Value("${app.jwt.refresh-token-validity-in-seconds:2592000}")
    Long refreshTokenExpiration;

    @PostMapping("/login")
    @ApiMessage("Login successful")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            @RequestHeader(value = "X-device-Id", required = false) String deviceId) {

        LoginResult result = this.authService.login(loginRequestDTO, deviceId);
        ResponseCookie cookie = CookieUtils.createRefreshTokenCookie(result.getRefreshToken(), refreshTokenExpiration);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.getResponse());
    }

    @PostMapping("/register")
    @ApiMessage("Register successful")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        String username = this.userService.register(registerRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Registration successful",
                "username", username
        ));
    }

    @PostMapping("/refresh-token")
    @ApiMessage("Refresh token successful")
    public ResponseEntity<LoginResponseDTO> refreshToken(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String bearerToken,
            @CookieValue(value = TokenConstants.REFRESH_TOKEN, required = false) String refreshToken) {

        String accessToken = SecurityUtils.extractBearerToken(bearerToken);
        LoginResult result = this.authService.getNewToken(refreshToken, accessToken);
        ResponseCookie cookie = CookieUtils.createRefreshTokenCookie(result.getRefreshToken(), refreshTokenExpiration);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.getResponse());
    }

    @PostMapping("/logout")
    @ApiMessage("Logout successful")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String bearerToken,
            @CookieValue(value = TokenConstants.REFRESH_TOKEN, required = false) String refreshToken) {

        String accessToken = SecurityUtils.extractBearerToken(bearerToken);
        this.authService.logout(refreshToken, accessToken);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, CookieUtils.deleteRefreshTokenCookie().toString())
                .build();
    }

    @GetMapping("/me")
    @ApiMessage("Fetch current user profile")
    public ResponseEntity<LoginResponseDTO.UserLoginResponseDTO> getCurrentUser() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(this.userService.getProfile(currentUserId));
    }
}
