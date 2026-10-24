package testmq.demo.service;

import testmq.demo.dto.LoginResult;
import testmq.demo.dto.request.auth.LoginRequestDTO;

public interface AuthService {
    LoginResult login(LoginRequestDTO request, String deviceId);
    LoginResult getNewToken(String refreshToken, String accessToken);
    void logout(String refreshToken, String accessToken);
}
