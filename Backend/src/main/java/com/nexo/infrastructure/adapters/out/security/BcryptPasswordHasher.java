package com.nexo.infrastructure.adapters.out.security;

import com.nexo.application.ports.out.PasswordHasherPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements PasswordHasherPort {
    private final PasswordEncoder encoder;

    public BcryptPasswordHasher(PasswordEncoder encoder) { this.encoder = encoder; }
    public String hash(String rawPassword) { return encoder.encode(rawPassword); }
    public boolean matches(String rawPassword, String passwordHash) { return encoder.matches(rawPassword, passwordHash); }
}
