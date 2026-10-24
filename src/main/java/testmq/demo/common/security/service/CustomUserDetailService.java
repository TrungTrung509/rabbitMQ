package testmq.demo.common.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import testmq.demo.entity.User;
import testmq.demo.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findByUsername(username)
                .or(() -> this.userRepository.findByEmail(username))
                .orElseThrow(() -> {
                    log.error("User not found with identifier: {}", username);
                    return new UsernameNotFoundException("User not found with username or email: " + username);
                });

        return new CustomUserDetail(user);
    }
}
