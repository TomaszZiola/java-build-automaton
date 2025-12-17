package io.github.tomaszziola.javabuildautomaton.api.exception;

import static java.time.Instant.now;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import io.github.tomaszziola.javabuildautomaton.api.dto.ErrorResponse;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.BuildNotFoundException;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.WorkspaceException;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ProjectNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleProjectNotFound(
      ProjectNotFoundException exception, HttpServletRequest request) {
    return buildErrorResponse(NOT_FOUND, "Not Found", exception, request);
  }

  @ExceptionHandler(BuildNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleBuildNotFound(
      BuildNotFoundException exception, HttpServletRequest request) {
    return buildErrorResponse(NOT_FOUND, "Not Found", exception, request);
  }

  @ExceptionHandler(WorkspaceException.class)
  public ResponseEntity<ErrorResponse> handleWorkspaceException(
      WorkspaceException exception, HttpServletRequest request) {
    return buildErrorResponse(BAD_REQUEST, "Workspace not prepared", exception, request);
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(
      HttpStatus status, String error, Exception exception, HttpServletRequest request) {
    var errorResponse =
        new ErrorResponse(
            now(), status.value(), error, exception.getMessage(), request.getRequestURI());
    return new ResponseEntity<>(errorResponse, status);
  }
}
