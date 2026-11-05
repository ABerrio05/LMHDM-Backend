package com.nexo.application.service;

import com.nexo.application.ports.out.EmailDeliveryPort;
import com.nexo.application.ports.in.NotificationPreferenceUseCase;
import com.nexo.domain.model.CollaborativeSpace;
import com.nexo.domain.model.SpaceMember;
import com.nexo.domain.model.Task;
import com.nexo.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/** Correos transaccionales informativos. Un fallo de entrega nunca revierte la operación de negocio. */
public class ActivityEmailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(ActivityEmailNotificationService.class);
    private final EmailDeliveryPort delivery;
    private final NotificationPreferenceUseCase preferences;
    private final Executor deliveryExecutor;

    public ActivityEmailNotificationService(EmailDeliveryPort delivery) { this(delivery, null, Runnable::run); }
    public ActivityEmailNotificationService(EmailDeliveryPort delivery, NotificationPreferenceUseCase preferences) {
        this(delivery, preferences, Runnable::run);
    }
    public ActivityEmailNotificationService(EmailDeliveryPort delivery, NotificationPreferenceUseCase preferences,
                                            Executor deliveryExecutor) {
        this.delivery = delivery;
        this.preferences = preferences;
        this.deliveryExecutor = deliveryExecutor;
    }

    /** Confirmación transaccional: se envía siempre al crear una cuenta. */
    public void accountCreated(User user) {
        send(user, "NEXO: cuenta creada correctamente",
                "Hola " + user.name() + ",\n\n"
                        + "Tu cuenta de NEXO fue creada correctamente. Ya puedes organizar tus tareas, notas y espacios colaborativos.\n\n"
                        + "Bienvenido/a a NEXO.", "account_created");
    }

    public void taskCreated(User owner, Task task) {
        String dueDate = task.dueDate() == null ? "Sin fecha límite" : task.dueDate().toString();
        sendActivity(owner, "NEXO: tarea creada - " + task.title(),
                "Hola " + owner.name() + ",\n\n"
                        + "Tu tarea fue creada correctamente en NEXO.\n\n"
                        + "Tarea: " + task.title() + "\n"
                        + "Prioridad: " + task.priority() + "\n"
                        + "Fecha límite: " + dueDate + "\n\n"
                        + "Ingresa a NEXO para organizarla.", "task_created");
    }

    public void taskUpdated(User owner, Task task) {
        sendActivity(owner, "NEXO: tarea actualizada - " + task.title(),
                "Hola " + owner.name() + ",\n\n"
                        + "Se actualizaron los datos de tu tarea.\n\nTarea: " + task.title()
                        + "\nPrioridad: " + task.priority() + "\n\nIngresa a NEXO para revisarla.", "task_updated");
    }

    public void taskDeleted(User owner, Task task) {
        sendActivity(owner, "NEXO: tarea eliminada - " + task.title(),
                "Hola " + owner.name() + ",\n\n"
                        + "La tarea \"" + task.title() + "\" fue eliminada de NEXO.\n\n"
                        + "Si fue un error, puedes crearla nuevamente desde Mis tareas.", "task_deleted");
    }

    public void sharedTaskCreated(User recipient, User creator, Task task) {
        sendActivity(recipient, "NEXO: nueva tarea compartida - " + task.title(),
                "Hola " + recipient.name() + ",\n\n"
                        + creator.name() + " creó una nueva tarea en uno de tus espacios colaborativos.\n\n"
                        + "Tarea: " + task.title() + "\n"
                        + "Prioridad: " + task.priority() + "\n\n"
                        + "Ingresa a NEXO para verla y coordinar el trabajo.", "shared_task_created");
    }

    public void sharedTaskCompleted(User recipient, User completedBy, Task task) {
        sendActivity(recipient, "NEXO: tarea compartida completada - " + task.title(),
                "Hola " + recipient.name() + ",\n\n"
                        + completedBy.name() + " marcó como completada una tarea de tu espacio colaborativo.\n\n"
                        + "Tarea: " + task.title() + "\n\n"
                        + "Ingresa a NEXO para revisar el avance del equipo.", "shared_task_completed");
    }

    public void sharedTaskUpdated(User recipient, User updatedBy, Task task) {
        sendActivity(recipient, "NEXO: tarea compartida actualizada - " + task.title(),
                "Hola " + recipient.name() + ",\n\n"
                        + updatedBy.name() + " actualizó una tarea de tu espacio colaborativo.\n\nTarea: " + task.title()
                        + "\n\nIngresa a NEXO para ver los cambios.", "shared_task_updated");
    }

    public void sharedTaskDeleted(User recipient, User deletedBy, Task task) {
        sendActivity(recipient, "NEXO: tarea compartida eliminada - " + task.title(),
                "Hola " + recipient.name() + ",\n\n"
                        + deletedBy.name() + " eliminó una tarea de tu espacio colaborativo.\n\nTarea: " + task.title(),
                "shared_task_deleted");
    }

    public void spaceCreated(User creator, CollaborativeSpace space) {
        sendActivity(creator, "NEXO: espacio colaborativo creado - " + space.name(),
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
                        + "La invitación vence en 2 horas.", "space_registration_invitation");
    }

    private void send(User recipient, String subject, String body, String event) {
        sendTo(recipient.email(), recipient.id(), subject, body, event);
    }

    private void sendActivity(User recipient, String subject, String body, String event) {
        if (preferences != null && !preferences.activityEmailsEnabledFor(recipient.id())) {
            log.info("activity_email_skipped event={} recipientUserId={} reason=user_preference", event, recipient.id());
            return;
        }
        send(recipient, subject, body, event);
    }

    private void sendTo(String recipientEmail, Long recipientUserId, String subject, String body, String event) {
        if (!delivery.isConfigured()) {
            log.info("activity_email_skipped event={} recipientUserId={} reason=mail_not_configured", event, recipientUserId);
            return;
        }
        deliveryExecutor.execute(() -> {
            try {
                delivery.send(recipientEmail, subject, body);
                log.info("activity_email_sent event={} recipientUserId={}", event, recipientUserId);
            } catch (RuntimeException exception) {
                log.warn("activity_email_failed event={} recipientUserId={}", event, recipientUserId, exception);
            }
        });
    }

    private String blankAsNotProvided(String value) {
        return value == null || value.isBlank() ? "No proporcionada" : value;
    }
}
