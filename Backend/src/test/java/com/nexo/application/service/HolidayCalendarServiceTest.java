package com.nexo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nexo.application.ports.out.HolidayCalendarProviderPort;
import com.nexo.domain.model.PublicHoliday;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class HolidayCalendarServiceTest {
    @Test
    void delegatesTheCalendarQueryUsingColombiasCountryCode() {
        CapturingHolidayProvider provider = new CapturingHolidayProvider();
        HolidayCalendarService service = new HolidayCalendarService(provider);

        List<PublicHoliday> holidays = service.listUpcomingColombianHolidays();

        assertEquals("CO", provider.countryCode);
        assertEquals(1, holidays.size());
        assertEquals("Día de prueba", holidays.getFirst().localName());
    }

    private static final class CapturingHolidayProvider implements HolidayCalendarProviderPort {
        private String countryCode;
        @Override public List<PublicHoliday> findUpcomingHolidays(String countryCode) {
            this.countryCode = countryCode;
            return List.of(new PublicHoliday(LocalDate.of(2026, 12, 25), "Día de prueba", countryCode));
        }
    }
}
