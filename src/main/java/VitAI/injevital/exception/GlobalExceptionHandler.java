package VitAI.injevital.exception;

import VitAI.injevital.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.login.LoginException;

/**
 * 전역 예외 처리를 담당하는 핸들러
 * 모든 컨트롤러에서 발생하는 예외를 ApiResponse 형식으로 통일하여 반환합니다.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 운동 정보를 찾을 수 없을 때 발생하는 예외 처리
     */
    @ExceptionHandler(ExerciseNotFoundException.class)
    public ResponseEntity<ApiResponse> handleExerciseNotFoundException(ExerciseNotFoundException e) {
        log.error("운동 정보를 찾을 수 없습니다: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
    }

    /**
     * 잘못된 운동 요청 예외 처리
     */
    @ExceptionHandler(InvalidExerciseRequestException.class)
    public ResponseEntity<ApiResponse> handleInvalidExerciseRequestException(InvalidExerciseRequestException e) {
        log.error("잘못된 운동 요청입니다: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    }

    /**
     * 로그인 실패 예외 처리
     */
    @ExceptionHandler({LoginException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse> handleLoginException(Exception e) {
        log.error("로그인 실패: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("아이디 또는 비밀번호가 일치하지 않습니다."));
    }

    /**
     * 인증 관련 예외 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthenticationException(AuthenticationException e) {
        log.error("인증 실패: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("인증에 실패했습니다."));
    }

    /**
     * IllegalArgumentException 예외 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("잘못된 인자입니다: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    }

    /**
     * 일반 예외 처리 (모든 예외의 최종 처리)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {
        log.error("서버 내부 오류가 발생했습니다: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}
