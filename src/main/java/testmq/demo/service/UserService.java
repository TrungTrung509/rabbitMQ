package testmq.demo.service;

import testmq.demo.dto.request.auth.RegisterRequestDTO;
import testmq.demo.dto.response.auth.LoginResponseDTO;
import testmq.demo.entity.User;

import java.util.UUID;

public interface UserService {
    User getUserByUsernameOrEmail(String identifier);
    User getUserById(UUID id);
    String register(RegisterRequestDTO registerRequestDTO);
    LoginResponseDTO.UserLoginResponseDTO getProfile(UUID userId);
}
