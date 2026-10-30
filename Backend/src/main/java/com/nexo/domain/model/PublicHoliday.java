package com.nexo.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/** Día festivo consultado desde un proveedor de calendario externo. */
public record PublicHoliday(LocalDate date, String localName, String countryCode) {
    public PublicHoliday {
        Objects.requireNonNull(date, "La fecha del festivo es obligatoria");
        Objects.requireNonNull(localName, "El nombre del festivo es obligatorio");
        Objects.requireNonNull(countryCode, "El país es obligatorio");
    }
}
