package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.service.AuthenticationService.EmailAlreadyRegisteredException;
import com.nexo.application.service.AuthenticationService.InvalidCredentialsException;
import com.nexo.application.service.TaskManagementService.TaskAccessDeniedException;
import com.nexo.application.service.TaskManagementService.TaskNotFoundException;
import com.nexo.application.service.ReminderManagementService.ReminderNotFoundException;
import com.nexo.application.service.CollaborativeSpaceService.SpaceAccessDeniedException;
import com.nexo.application.service.CollaborativeSpaceService.SpaceNotFoundException;
import com.nexo.application.service.CollaborativeSpaceService.UserNotFoundException;
import com.nexo.application.service.CollaborativeSpaceService.MemberAlreadyExistsException;
import java.util.LinkedHashMap;
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
    @ExceptionHandler(TaskNotFoundException.class)
    ResponseEntity<Map<String, String>> taskNotFound() { return error(HttpStatus.NOT_FOUND, "Tarea no encontrada"); }
    @ExceptionHandler(TaskAccessDeniedException.class)
    ResponseEntity<Map<String, String>> taskAccessDenied() { return error(HttpStatus.FORBIDDEN, "No tienes acceso a esta tarea"); }
    @ExceptionHandler(ReminderNotFoundException.class)
    ResponseEntity<Map<String, String>> reminderNotFound() { return error(HttpStatus.NOT_FOUND, "Recordatorio no encontrado"); }
    @ExceptionHandler(SpaceNotFoundException.class)
    ResponseEntity<Map<String, String>> spaceNotFound() { return error(HttpStatus.NOT_FOUND, "Espacio no encontrado"); }
    @ExceptionHandler(SpaceAccessDeniedException.class)
    ResponseEntity<Map<String, String>> spaceAccessDenied() { return error(HttpStatus.FORBIDDEN, "No tienes acceso a este espacio"); }
    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<Map<String, String>> userNotFound() { return error(HttpStatus.NOT_FOUND, "Usuario no encontrado"); }
    @ExceptionHandler(MemberAlreadyExistsException.class)
    ResponseEntity<Map<String, String>> memberAlreadyExists() { return error(HttpStatus.CONFLICT, "El usuario ya pertenece al espacio"); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validationError(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of(
                "message", "Datos de entrada inválidos",
                "errors", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> invalidRequest() { return error(HttpStatus.BAD_REQUEST, "Solicitud inválida"); }
    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
