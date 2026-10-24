package testmq.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import testmq.demo.common.utils.annotation.ApiMessage;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/hello")
    @ApiMessage("Hello authenticated user")
    public ResponseEntity<Map<String, Object>> hello(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "message", "Hello! You are successfully authenticated with JWT.",
                "user", authentication.getName(),
                "authorities", authentication.getAuthorities()
        ));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Admin only endpoint")
    public ResponseEntity<Map<String, String>> adminOnly() {
        return ResponseEntity.ok(Map.of(
                "message", "Access granted to admin-only area."
        ));
    }
}
