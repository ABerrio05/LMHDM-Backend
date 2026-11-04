package com.nexo.application.service;

import com.nexo.application.ports.out.EmailDeliveryPort;
import com.nexo.domain.model.CollaborativeSpace;
import com.nexo.domain.model.SpaceMember;
import com.nexo.domain.model.Task;
import com.nexo.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Correos transaccionales informativos. Un fallo de entrega nunca revierte la operación de negocio. */
public class ActivityEmailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(ActivityEmailNotificationService.class);
    private final EmailDeliveryPort delivery;

    public ActivityEmailNotificationService(EmailDeliveryPort delivery) { this.delivery = delivery; }

    public void taskCreated(User owner, Task task) {
        String dueDate = task.dueDate() == null ? "Sin fecha límite" : task.dueDate().toString();
        send(owner, "NEXO: tarea creada - " + task.title(),
                "Hola " + owner.name() + ",\n\n"
                        + "Tu tarea fue creada correctamente en NEXO.\n\n"
                        + "Tarea: " + task.title() + "\n"
                        + "Prioridad: " + task.priority() + "\n"
                        + "Fecha límite: " + dueDate + "\n\n"
                        + "Ingresa a NEXO para organizarla.", "task_created");
    }

    public void spaceCreated(User creator, CollaborativeSpace space) {
        send(creator, "NEXO: espacio colaborativo creado - " + space.name(),
                "Hola " + creator.name() + ",\n\n"
                        + "Creaste correctamente el espacio colaborativo: " + space.name() + ".\n"
                        + "Descripción: " + blankAsNotProvided(space.description()) + "\n\n"
                        + "Ya puedes invitar integrantes y organizar tareas compartidas.", "space_created");
    }

    public void spaceInvitation(User invitedUser, User inviter, CollaborativeSpace space, SpaceMember.Role role) {
        send(invitedUser, "NEXO: invitación al espacio " + space.name(),
                "Hola " + invitedUser.name() + ",\n\n"
                        + inviter.name() + " te invitó al espacio colaborativo \"" + space.name() + "\".\n"
                        + "Rol asignado: " + role + "\n"
                        + "Descripción: " + blankAsNotProvided(space.description()) + "\n\n"
                        + "Inicia sesión en NEXO para ver el espacio y sus tareas.", "space_invitation");
    }

    public void pendingSpaceInvitation(User inviter, CollaborativeSpace space, String email, SpaceMember.Role role,
                                       String registrationUrl) {
        sendTo(email, inviter.id(), "NEXO: invitación para registrarte en " + space.name(),
                "Hola,\n\n" + inviter.name() + " te invitó al espacio colaborativo \"" + space.name() + "\" en NEXO.\n"
                        + "Rol asignado: " + role + "\n\n"
                        + "Para aceptar la invitación, crea tu cuenta con este mismo correo usando este enlace:\n"
                        + registrationUrl + "\n\n"
                        + "La invitación vence en 7 días.", "space_registration_invitation");
    }

    private void send(User recipient, String subject, String body, String event) {
        sendTo(recipient.email(), recipient.id(), subject, body, event);
    }

    private void sendTo(String recipientEmail, Long recipientUserId, String subject, String body, String event) {
        if (!delivery.isConfigured()) {
            log.info("activity_email_skipped event={} recipientUserId={} reason=mail_not_configured", event, recipientUserId);
            return;
        }
        try {
            delivery.send(recipientEmail, subject, body);
            log.info("activity_email_sent event={} recipientUserId={}", event, recipientUserId);
        } catch (RuntimeException exception) {
            log.warn("activity_email_failed event={} recipientUserId={}", event, recipientUserId, exception);
        }
    }

    private String blankAsNotProvided(String value) {
        return value == null || value.isBlank() ? "No proporcionada" : value;
    }
}
