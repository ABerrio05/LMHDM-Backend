package com.nexo.infrastructure.adapters.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void mapsDomainExceptionsToExpectedHttpResponses() {
        assertError(handler.emailAlreadyRegistered(), HttpStatus.CONFLICT, "El correo ya está registrado");
        assertError(handler.invalidCredentials(), HttpStatus.UNAUTHORIZED, "Correo o contraseña inválidos");
        assertError(handler.accountInactive(), HttpStatus.FORBIDDEN,
                "Esta cuenta está desactivada. Contacta al administrador.");
        assertError(handler.platformUserNotFound(), HttpStatus.NOT_FOUND, "Usuario no encontrado");
        assertError(handler.taskNotFound(), HttpStatus.NOT_FOUND, "Tarea no encontrada");
        assertError(handler.taskAccessDenied(), HttpStatus.FORBIDDEN, "No tienes acceso a esta tarea");
        assertError(handler.reminderNotFound(), HttpStatus.NOT_FOUND, "Recordatorio no encontrado");
        assertError(handler.spaceNotFound(), HttpStatus.NOT_FOUND, "Espacio no encontrado");
        assertError(handler.spaceAccessDenied(), HttpStatus.FORBIDDEN, "No tienes acceso a este espacio");
        assertError(handler.userNotFound(), HttpStatus.NOT_FOUND, "Usuario no encontrado");
        assertError(handler.memberAlreadyExists(), HttpStatus.CONFLICT, "El usuario ya pertenece al espacio");
        assertError(handler.holidayProviderUnavailable(), HttpStatus.SERVICE_UNAVAILABLE,
                "El calendario externo no está disponible. Intenta nuevamente.");
        assertError(handler.noteNotFound(), HttpStatus.NOT_FOUND, "Nota no encontrada");
        assertError(handler.noteAccessDenied(), HttpStatus.FORBIDDEN, "No tienes acceso a esta nota");
        assertError(handler.assistantUnavailable(), HttpStatus.SERVICE_UNAVAILABLE,
                "El asistente de IA no está disponible. Intenta nuevamente.");
        assertError(handler.invalidRequest(), HttpStatus.BAD_REQUEST, "Solicitud inválida");
    }

    @Test
    void mapsValidationErrorsByField() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "Debe ser un correo válido"));

        ResponseEntity<Map<String, Object>> response = handler.validationError(
                new MethodArgumentNotValidException(null, bindingResult));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Datos de entrada inválidos", response.getBody().get("message"));
        assertEquals(Map.of("email", "Debe ser un correo válido"), response.getBody().get("errors"));
    }

    private void assertError(ResponseEntity<Map<String, String>> response, HttpStatus status, String message) {
        assertEquals(status, response.getStatusCode());
        assertEquals(message, response.getBody().get("message"));
    }
}
