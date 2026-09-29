package com.nexo.infrastructure.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexo.application.ports.in.AuthenticationUseCase;
import com.nexo.application.ports.out.TokenProviderPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Prueba de integración de la capa HTTP: petición JSON, validación y respuesta del endpoint. */
@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private AuthenticationUseCase authentication;
    @MockBean private TokenProviderPort tokenProvider;

    @Test
    void registersUserThroughHttpEndpoint() throws Exception {
        when(authentication.register(any())).thenReturn(
                new AuthenticationUseCase.AuthenticationResult(15L, "Ana", "ana@nexo.test", "jwt-de-prueba"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"ana@nexo.test\",\"password\":\"ClaveSegura1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(15))
                .andExpect(jsonPath("$.email").value("ana@nexo.test"))
                .andExpect(jsonPath("$.accessToken").value("jwt-de-prueba"));

        verify(authentication).register(any());
    }

    @Test
    void explainsWhichRegistrationFieldIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"ana@nexo.test\",\"password\":\"a\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Datos de entrada inválidos"))
                .andExpect(jsonPath("$.errors.password").value("La contraseña debe tener entre 8 y 72 caracteres"));
    }
}
