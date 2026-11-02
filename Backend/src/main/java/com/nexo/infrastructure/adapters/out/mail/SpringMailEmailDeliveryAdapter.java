package com.nexo.infrastructure.adapters.out.mail;

import com.nexo.application.ports.out.EmailDeliveryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.notifications.email", name = "enabled", havingValue = "true")
public class SpringMailEmailDeliveryAdapter implements EmailDeliveryPort {
    private final JavaMailSender mailSender;
    private final String from;
    private final String host;

    public SpringMailEmailDeliveryAdapter(JavaMailSender mailSender,
                                          @Value("${app.notifications.email.from}") String from,
                                          @Value("${spring.mail.host:}") String host) {
        this.mailSender = mailSender;
        this.from = from;
        this.host = host;
    }

    @Override public boolean isConfigured() { return !from.isBlank() && !host.isBlank(); }

    @Override public void send(String recipient, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }
}
