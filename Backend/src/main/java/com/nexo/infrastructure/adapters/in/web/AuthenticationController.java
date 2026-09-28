package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.ports.in.AuthenticationUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationUseCase authentication;

    public AuthenticationController(AuthenticationUseCase authentication) { this.authentication = authentication; }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationUseCase.AuthenticationResult> register(@Valid @RequestBody RegisterRequest request) {
        var result = authentication.register(new AuthenticationUseCase.RegisterCommand(request.name(), request.email(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationUseCase.AuthenticationResult> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authentication.login(new AuthenticationUseCase.LoginCommand(request.email(), request.password())));
    }

    public record RegisterRequest(@NotBlank @Size(max = 150) String name, @NotBlank @Email String email,
                                  @NotBlank @Size(min = 8, max = 72) String password) { }
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) { }
}
