package testmq.demo.common.utils;

import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseCookie;
import testmq.demo.common.constant.TokenConstants;

@UtilityClass
public class CookieUtils {

    public ResponseCookie createRefreshTokenCookie(String refreshToken, long maxAgeSeconds) {
        return ResponseCookie.from(TokenConstants.REFRESH_TOKEN, refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(TokenConstants.REFRESH_TOKEN, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }
}
