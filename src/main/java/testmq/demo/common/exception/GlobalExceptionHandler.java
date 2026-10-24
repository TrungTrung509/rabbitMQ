package testmq.demo.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import testmq.demo.dto.response.ApiResponse;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IdInvalidException.class)
    public ResponseEntity<ApiResponse<Object>> handleIdInvalidException(IdInvalidException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid ID Exception", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        Object message = errors.size() > 1 ? errors : (errors.isEmpty() ? "Validation failed" : errors.get(0));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", message);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "User Not Found", e.getMessage());
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            org.springframework.security.authentication.BadCredentialsException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(Exception e) {
        String msg = (e.getMessage() != null && !e.getMessage().isBlank()) ? e.getMessage() : "Invalid credentials";
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed", msg);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(IllegalStateException e) {
        String message = e.getMessage();
        String customMessage = message;
        if (message != null) {
            if (message.contains("already exists") || message.contains("already in use")) {
                customMessage = "Resource is already in use or exists.";
            } else if (message.contains("cannot be modified") || message.contains("cannot be changed")) {
                customMessage = "Resource cannot be modified in its current state.";
            } else if (message.contains("invalid state") || message.contains("not allowed")) {
                customMessage = "Operation not allowed in current state.";
            }
        }
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid State", customMessage);
    }

    @ExceptionHandler({ResourceNotFoundException.class, NotFoundException.class})
    public ResponseEntity<ApiResponse<Object>> handleNotFoundException(RuntimeException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource Not Found", e.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResourceException(DuplicateResourceException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Duplicate Resource", e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Business Rule Violation", e.getMessage());
    }

    @ExceptionHandler(TokenExpiredOrUsedException.class)
    public ResponseEntity<ApiResponse<Object>> handleTokenExpiredOrUsedException(TokenExpiredOrUsedException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Token Expired or Invalid", e.getMessage());
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleLockedException(LockedException e) {
        return buildErrorResponse(HttpStatus.LOCKED, "Account Locked", "Your account has been locked. Please contact administrator.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access Denied", "You don't have permission to access this resource");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Missing Request Parameter", "Required parameter '" + e.getParameterName() + "' is missing");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Type Mismatch", "Invalid value for parameter '" + e.getName() + "'");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Request Body", "Malformed JSON request or invalid data format");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Endpoint Not Found", "The requested endpoint does not exist: " + e.getRequestURL());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Database Constraint Violation", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e) {
        log.error("Unhandled exception caught: ", e);
        String msg = "prod".equals(System.getProperty("env"))
                ? "An unexpected error occurred. Please try again later."
                : e.getMessage();
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", msg);
    }

    private ResponseEntity<ApiResponse<Object>> buildErrorResponse(HttpStatus status, String error, Object message) {
        return ResponseEntity.status(status).body(ApiResponse.builder()
                .statusCode(status.value())
                .error(error)
                .message(message)
                .build());
    }
}
