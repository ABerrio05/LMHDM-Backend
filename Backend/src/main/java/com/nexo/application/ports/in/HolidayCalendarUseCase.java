package com.nexo.application.ports.in;

import com.nexo.domain.model.PublicHoliday;
import java.util.List;

/** Consulta de fechas útiles para planificar tareas; no depende de HTTP ni del proveedor externo. */
public interface HolidayCalendarUseCase {
    List<PublicHoliday> listUpcomingColombianHolidays();
}
