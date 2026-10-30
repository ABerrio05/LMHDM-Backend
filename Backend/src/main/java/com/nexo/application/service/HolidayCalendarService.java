package com.nexo.application.service;

import com.nexo.application.ports.in.HolidayCalendarUseCase;
import com.nexo.application.ports.out.HolidayCalendarProviderPort;
import com.nexo.domain.model.PublicHoliday;
import java.util.List;

public class HolidayCalendarService implements HolidayCalendarUseCase {
    private static final String COLOMBIA = "CO";
    private final HolidayCalendarProviderPort holidayProvider;

    public HolidayCalendarService(HolidayCalendarProviderPort holidayProvider) {
        this.holidayProvider = holidayProvider;
    }

    @Override
    public List<PublicHoliday> listUpcomingColombianHolidays() {
        return holidayProvider.findUpcomingHolidays(COLOMBIA);
    }
}
