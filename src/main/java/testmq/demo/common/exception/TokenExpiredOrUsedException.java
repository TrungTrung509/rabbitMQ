package testmq.demo.common.exception;

public class TokenExpiredOrUsedException extends RuntimeException {
    public TokenExpiredOrUsedException(String message) {
        super(message);
    }
}
