package com.nexo.domain.model;

import java.time.LocalDateTime;

/** Preferencia opcional para correos informativos; recordatorios e invitaciones no dependen de ella. */
public record NotificationPreference(Long userId, boolean activityEmailsEnabled, LocalDateTime updatedAt) { }
