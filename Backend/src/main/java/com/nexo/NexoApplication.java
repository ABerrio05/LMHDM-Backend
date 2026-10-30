package com.nexo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/** Punto de entrada de la API; la autenticación se resuelve con JWT propio. */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class NexoApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexoApplication.class, args);
    }
}
