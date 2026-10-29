package com.nexo.application.ports.out;

import com.nexo.domain.model.PublicHoliday;
import java.util.List;

/** Puerto para proveedores externos de calendario. */
public interface HolidayCalendarProviderPort {
    List<PublicHoliday> findUpcomingHolidays(String countryCode);
}
