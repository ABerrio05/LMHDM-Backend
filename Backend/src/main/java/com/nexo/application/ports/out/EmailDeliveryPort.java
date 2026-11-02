package com.nexo.application.ports.out;

/** Puerto para no acoplar la lógica de recordatorios a un proveedor SMTP concreto. */
public interface EmailDeliveryPort {
    boolean isConfigured();
    void send(String recipient, String subject, String content);
}
