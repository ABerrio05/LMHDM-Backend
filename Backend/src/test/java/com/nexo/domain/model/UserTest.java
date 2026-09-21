package com.nexo.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class UserTest {
    @Test
    void normalizesEmailBeforePersistingIt() {
        User user = new User(null, "Ana", " ANA@EXAMPLE.COM ", "bcrypt-hash", LocalDateTime.now());

        assertEquals("ana@example.com", user.email());
    }
}
