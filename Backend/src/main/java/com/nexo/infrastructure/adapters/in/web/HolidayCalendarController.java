package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.ports.in.HolidayCalendarUseCase;
import com.nexo.domain.model.PublicHoliday;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calendar")
@Tag(name = "Planificación", description = "Fechas externas útiles para planear tareas")
@SecurityRequirement(name = "bearerAuth")
public class HolidayCalendarController {
    private final HolidayCalendarUseCase holidays;

    public HolidayCalendarController(HolidayCalendarUseCase holidays) {
        this.holidays = holidays;
    }

    @GetMapping("/holidays/colombia")
    @Operation(summary = "Consultar próximos festivos de Colombia")
    public List<PublicHoliday> upcomingColombianHolidays() {
        return holidays.listUpcomingColombianHolidays();
    }
}
