package io.github.tomaszziola.javabuildautomaton.api.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import io.github.tomaszziola.javabuildautomaton.api.dto.ErrorResponse;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ProjectNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleProjectNotFound(
      final ProjectNotFoundException exception, final HttpServletRequest request) {
    final var errorResponse =
        new ErrorResponse(
            LocalDateTime.now(),
            NOT_FOUND.value(),
            "Not Found",
            exception.getMessage(),
            request.getRequestURI());
    return new ResponseEntity<>(errorResponse, NOT_FOUND);
  }
}
