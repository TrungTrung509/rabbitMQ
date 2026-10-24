package testmq.demo.common.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import testmq.demo.common.enums.RoleEnum;
import testmq.demo.common.enums.StatusEnum;
import testmq.demo.entity.Role;
import testmq.demo.entity.User;
import testmq.demo.repository.RoleRepository;
import testmq.demo.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            Role adminRole = this.roleRepository.findByCode("ROLE_ADMIN")
                    .or(() -> this.roleRepository.findByName(RoleEnum.ADMIN.name()))
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName(RoleEnum.ADMIN.name());
                        role.setCode("ROLE_ADMIN");
                        role.setDescription("Administrator with full privileges");
                        role.setStatus(StatusEnum.ACTIVE);
                        return this.roleRepository.save(role);
                    });

            Role userRole = this.roleRepository.findByCode("ROLE_USER")
                    .or(() -> this.roleRepository.findByName(RoleEnum.USER.name()))
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName(RoleEnum.USER.name());
                        role.setCode("ROLE_USER");
                        role.setDescription("Standard User");
                        role.setStatus(StatusEnum.ACTIVE);
                        return this.roleRepository.save(role);
                    });

            if (this.userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("superadmin123");
                admin.setEmail("superadmin@gmail.com");
                admin.setPassword(this.passwordEncoder.encode("SuperAdmin123@"));
                admin.setFirstName("Super");
                admin.setLastName("Admin");
                admin.setPhone("0987654321");
                admin.setStatus(StatusEnum.ACTIVE);
                admin.setRole(adminRole);
                admin.setCreatedBy("INIT_RUNNER");
                this.userRepository.save(admin);
                log.info("Initialized default admin user: superadmin123 / SuperAdmin123@");

                User normalUser = new User();
                normalUser.setUsername("user123");
                normalUser.setEmail("user@gmail.com");
                normalUser.setPassword(this.passwordEncoder.encode("User123@"));
                normalUser.setFirstName("John");
                normalUser.setLastName("Doe");
                normalUser.setPhone("0912345678");
                normalUser.setStatus(StatusEnum.ACTIVE);
                normalUser.setRole(userRole);
                normalUser.setCreatedBy("INIT_RUNNER");
                this.userRepository.save(normalUser);
                log.info("Initialized default normal user: user123 / User123@");
            }
        } catch (Exception e) {
            log.error("Error during database initialization: {}", e.getMessage(), e);
        }
    }
}
