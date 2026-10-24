package testmq.demo.common.security.jwt;

import com.nimbusds.jose.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import testmq.demo.common.constant.JwtConstants;
import testmq.demo.common.constant.TokenConstants;
import testmq.demo.common.security.service.CustomUserDetail;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {
    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.base64-secret}")
    private String jwtKey;

    @Value("${app.jwt.access-token-validity-in-seconds:900}")
    private Long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-validity-in-seconds:2592000}")
    private Long refreshTokenExpiration;

    public String createAccessToken(Authentication authentication, String deviceId) {
        CustomUserDetail userPrincipal = (CustomUserDetail) authentication.getPrincipal();
        JwsHeader jwtHeader = JwsHeader.with(JwtConstants.Header.ALGORITHM).build();
        Map<String, Object> extraClaims = buildCustomClaims(userPrincipal, deviceId, TokenConstants.ACCESS_TOKEN);

        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);
        String jti = UUID.randomUUID().toString();

        JwtClaimsSet jwtPayload = JwtClaimsSet.builder()
                .subject(String.valueOf(userPrincipal.getUser().getUserId()))
                .id(jti)
                .claims(claim -> claim.putAll(extraClaims))
                .issuedAt(now)
                .expiresAt(validity)
                .build();

        String accessToken = this.jwtEncoder.encode(JwtEncoderParameters.from(jwtHeader, jwtPayload)).getTokenValue();
        log.debug("Created access token for userId {}: {}", userPrincipal.getUser().getUserId(), accessToken);
        return accessToken;
    }

    public String createRefreshToken(Authentication authentication, String deviceId) {
        CustomUserDetail userPrincipal = (CustomUserDetail) authentication.getPrincipal();
        JwsHeader jwtHeader = JwsHeader.with(JwtConstants.Header.ALGORITHM).build();
        Map<String, Object> extraClaims = buildCustomClaims(userPrincipal, deviceId, TokenConstants.REFRESH_TOKEN);

        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);
        String jti = UUID.randomUUID().toString();

        JwtClaimsSet jwtPayload = JwtClaimsSet.builder()
                .subject(String.valueOf(userPrincipal.getUser().getUserId()))
                .id(jti)
                .claims(claim -> claim.putAll(extraClaims))
                .issuedAt(now)
                .expiresAt(validity)
                .build();

        String refreshToken = this.jwtEncoder.encode(JwtEncoderParameters.from(jwtHeader, jwtPayload)).getTokenValue();
        log.debug("Created refresh token for userId {}: {}", userPrincipal.getUser().getUserId(), refreshToken);
        return refreshToken;
    }

    public Jwt checkValidRefreshToken(String refreshToken) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JwtConstants.Header.ALGORITHM)
                .build();
        return jwtDecoder.decode(refreshToken);
    }

    public Jwt checkValidAccessToken(String accessToken) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JwtConstants.Header.ALGORITHM)
                .build();
        return jwtDecoder.decode(accessToken);
    }

    public String getJtiFromToken(String token) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JwtConstants.Header.ALGORITHM)
                .build();
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getId();
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JwtConstants.Header.ALGORITHM.getName());
    }

    private Map<String, Object> buildCustomClaims(CustomUserDetail userPrincipal, String deviceId, String tokenType) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(JwtConstants.Claims.FIRST_NAME, userPrincipal.getUser().getFirstName());
        extraClaims.put(JwtConstants.Claims.LAST_NAME, userPrincipal.getUser().getLastName());
        extraClaims.put(JwtConstants.Claims.ROLE, userPrincipal.getUser().getRole() != null ? userPrincipal.getUser().getRole().getCode() : "ROLE_USER");
        extraClaims.put(JwtConstants.Claims.USERNAME, userPrincipal.getUser().getUsername());
        extraClaims.put(JwtConstants.Claims.EMAIL, userPrincipal.getUser().getEmail());
        extraClaims.put(JwtConstants.Claims.TOKEN_TYPE, tokenType);
        extraClaims.put(JwtConstants.Claims.DEVICE_ID, deviceId != null ? deviceId : "unknown-device");
        return extraClaims;
    }
}
