package com.nexo.infrastructure.adapters.out.mail;

import com.nexo.application.ports.out.EmailDeliveryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Evita simular entregas cuando todavía no se han configurado credenciales SMTP. */
@Component
@ConditionalOnProperty(prefix = "app.notifications.email", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledEmailDeliveryAdapter implements EmailDeliveryPort {
    @Override public boolean isConfigured() { return false; }
    @Override public void send(String recipient, String subject, String content) {
        throw new IllegalStateException("El envío de correo no está configurado");
    }
}
