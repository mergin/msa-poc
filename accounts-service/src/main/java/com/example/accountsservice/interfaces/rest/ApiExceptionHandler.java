package com.example.accountsservice.interfaces.rest;

import com.example.accountsservice.interfaces.rest.exception.ResourceNotFoundException;
import com.example.accountsservice.interfaces.rest.exception.ServiceUnavailableException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("message", exception.getMessage()));
  }

  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<Map<String, String>> handleServiceUnavailable(
      ServiceUnavailableException exception) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of("message", exception.getMessage()));
  }
}
