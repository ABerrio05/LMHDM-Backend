package com.nexo.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "recurrencia")
public class RecurrenceJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_recurrencia") private Long id;
    @Column(name = "tipo", nullable = false) private String frequency;
    @Column(name = "intervalo", nullable = false) private int intervalValue;
    @Column(name = "fecha_fin") private LocalDate endDate;

    protected RecurrenceJpaEntity() { }
    RecurrenceJpaEntity(String frequency, int intervalValue, LocalDate endDate) {
        this.frequency = frequency; this.intervalValue = intervalValue; this.endDate = endDate;
    }
    Long getId() { return id; }
    String getFrequency() { return frequency; }
    int getIntervalValue() { return intervalValue; }
    LocalDate getEndDate() { return endDate; }
}
