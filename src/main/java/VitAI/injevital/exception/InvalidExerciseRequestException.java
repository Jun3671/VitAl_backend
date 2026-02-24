package VitAI.injevital.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidExerciseRequestException extends RuntimeException {
    public InvalidExerciseRequestException(String message) {
        super(message);
    }
}