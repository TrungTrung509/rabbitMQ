package testmq.demo.service.service;

import testmq.demo.dto.request.auth.RegisterRequestDTO;
import testmq.demo.dto.response.auth.RegisterResponseDTO;
import testmq.demo.dto.response.auth.UserLoginResponseDTO;
import testmq.demo.entity.User;

import java.util.UUID;

public interface UserService {
    UserLoginResponseDTO getUserByUsernameOrEmail(String identifier);
    UserLoginResponseDTO getUserById(UUID id);
    RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO);
    UserLoginResponseDTO getProfile(UUID userId);
    UserLoginResponseDTO toResponse(User user);
}
