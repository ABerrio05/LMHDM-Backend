package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.service.AuthenticationService.EmailAlreadyRegisteredException;
import com.nexo.application.service.AuthenticationService.InvalidCredentialsException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ResponseEntity<Map<String, String>> emailAlreadyRegistered() {
        return error(HttpStatus.CONFLICT, "El correo ya está registrado");
    }
    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<Map<String, String>> invalidCredentials() {
        return error(HttpStatus.UNAUTHORIZED, "Correo o contraseña inválidos");
    }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    ResponseEntity<Map<String, String>> invalidRequest() { return error(HttpStatus.BAD_REQUEST, "Solicitud inválida"); }
    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
