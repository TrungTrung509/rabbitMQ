package testmq.demo.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import testmq.demo.common.enums.RoleEnum;
import testmq.demo.common.enums.StatusEnum;
import testmq.demo.common.exception.DuplicateResourceException;
import testmq.demo.common.exception.IdInvalidException;
import testmq.demo.dto.request.auth.RegisterRequestDTO;
import testmq.demo.dto.response.auth.LoginResponseDTO;
import testmq.demo.entity.Role;
import testmq.demo.entity.User;
import testmq.demo.repository.RoleRepository;
import testmq.demo.repository.UserRepository;
import testmq.demo.service.UserService;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    public User getUserByUsernameOrEmail(String identifier) {
        return this.userRepository.findByUsername(identifier)
                .or(() -> this.userRepository.findByEmail(identifier))
                .orElseThrow(() -> {
                    log.debug("User not found with identifier: {}", identifier);
                    return new IdInvalidException("User not found");
                });
    }

    public User getUserById(UUID id) {
        return this.userRepository.findByUserId(id)
                .orElseThrow(() -> {
                    log.debug("User not found with id: {}", id);
                    return new IdInvalidException("User not found");
                });
    }

    @Transactional
    public String register(RegisterRequestDTO registerRequestDTO) {
        if (this.userRepository.existsByUsername(registerRequestDTO.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (this.userRepository.existsByEmail(registerRequestDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        Role userRole = this.roleRepository.findByName(RoleEnum.USER.name())
                .or(() -> this.roleRepository.findByCode("ROLE_USER"))
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name(RoleEnum.USER.name())
                            .code("ROLE_USER")
                            .description("Default User Role")
                            .status(StatusEnum.ACTIVE)
                            .build();
                    return this.roleRepository.save(newRole);
                });

        User user = User.builder()
                .username(registerRequestDTO.getUsername())
                .email(registerRequestDTO.getEmail())
                .password(this.passwordEncoder.encode(registerRequestDTO.getPassword()))
                .firstName(registerRequestDTO.getFirstName())
                .lastName(registerRequestDTO.getLastName())
                .phone(registerRequestDTO.getPhone())
                .role(userRole)
                .status(StatusEnum.ACTIVE)
                .createdBy("SYSTEM_REGISTRATION")
                .build();

        this.userRepository.save(user);
        log.info("Registered user successfully: {}", user.getUsername());
        return user.getUsername();
    }

    public LoginResponseDTO.UserLoginResponseDTO getProfile(UUID userId) {
        User user = getUserById(userId);
        return LoginResponseDTO.UserLoginResponseDTO.builder()
                .userId(String.valueOf(user.getUserId()))
                .fullName(user.getFullName())
                .username(user.getUsername())
                .roleCode(user.getRole() != null ? user.getRole().getCode() : "ROLE_USER")
                .avatarImage(user.getAvatarImage())
                .build();
    }
}
