package com.nexo.infrastructure.adapters.out.calendar;

import com.nexo.application.ports.out.HolidayCalendarProviderPort;
import com.nexo.domain.model.PublicHoliday;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/** Adaptador HTTP para Nager.Date. Mantiene el proveedor externo fuera de la capa de aplicación. */
@Component
public class NagerDateHolidayClient implements HolidayCalendarProviderPort {
    private static final Logger log = LoggerFactory.getLogger(NagerDateHolidayClient.class);
    private final RestClient client;

    public NagerDateHolidayClient(@Value("${app.calendar.base-url:https://date.nager.at/api/v3}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Override
    public List<PublicHoliday> findUpcomingHolidays(String countryCode) {
        try {
            NagerHoliday[] response = client.get()
                    .uri("/NextPublicHolidays/{countryCode}", countryCode)
                    .retrieve()
                    .body(NagerHoliday[].class);
            List<PublicHoliday> holidays = Arrays.stream(response == null ? new NagerHoliday[0] : response)
                    .map(holiday -> new PublicHoliday(holiday.date(), holiday.localName(), holiday.countryCode()))
                    .toList();
            log.info("external_calendar_success provider=NagerDate country={} count={}", countryCode, holidays.size());
            return holidays;
        } catch (RestClientException exception) {
            log.warn("external_calendar_unavailable provider=NagerDate country={} reason={}", countryCode,
                    exception.getClass().getSimpleName());
            throw new HolidayProviderUnavailableException();
        }
    }

    private record NagerHoliday(LocalDate date, String localName, String countryCode) { }

    public static class HolidayProviderUnavailableException extends RuntimeException { }
}
