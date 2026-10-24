package testmq.demo.common.constant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

public final class JwtConstants {
    private JwtConstants() {}

    public static final class Header {
        private Header() {}
        public static final MacAlgorithm ALGORITHM = MacAlgorithm.HS512;
    }

    public static final class Claims {
        private Claims() {}
        public static final String FIRST_NAME = "firstName";
        public static final String LAST_NAME = "lastName";
        public static final String ROLE = "role";
        public static final String USERNAME = "username";
        public static final String EMAIL = "email";
        public static final String TOKEN_TYPE = "tokenType";
        public static final String DEVICE_ID = "deviceId";
    }
}
