package com.nexo.infrastructure.adapters.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nexo.application.ports.in.HolidayCalendarUseCase;
import com.nexo.domain.model.PublicHoliday;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class HolidayCalendarControllerTest {

    @Test
    void returnsTheUpcomingHolidaysProvidedByTheUseCase() {
        List<PublicHoliday> expected = List.of(new PublicHoliday(LocalDate.of(2026, 1, 1), "Año Nuevo", "CO"));
        HolidayCalendarUseCase useCase = () -> expected;

        List<PublicHoliday> result = new HolidayCalendarController(useCase).upcomingColombianHolidays();

        assertEquals(expected, result);
    }
}
