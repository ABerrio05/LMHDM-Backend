package com.nexo.infrastructure.adapters.out.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nexo.domain.model.PublicHoliday;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NagerDateHolidayClientTest {
    private HttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void mapsTheExternalHolidayResponseToTheDomain() throws Exception {
        server.createContext("/NextPublicHolidays/CO", exchange -> respond(exchange, 200,
                "[{\"date\":\"2026-01-01\",\"localName\":\"Año Nuevo\",\"countryCode\":\"CO\"}]"));
        NagerDateHolidayClient client = new NagerDateHolidayClient(baseUrl());

        List<PublicHoliday> holidays = client.findUpcomingHolidays("CO");

        assertEquals(1, holidays.size());
        assertEquals("Año Nuevo", holidays.getFirst().localName());
        assertEquals("CO", holidays.getFirst().countryCode());
    }

    @Test
    void translatesAnUnavailableProviderToTheApplicationException() {
        server.createContext("/NextPublicHolidays/CO", exchange -> respond(exchange, 503, "unavailable"));
        NagerDateHolidayClient client = new NagerDateHolidayClient(baseUrl());

        assertThrows(NagerDateHolidayClient.HolidayProviderUnavailableException.class,
                () -> client.findUpcomingHolidays("CO"));
    }

    @Test
    void reusesTheCachedResponseWithoutCallingTheExternalProviderAgain() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/NextPublicHolidays/CO", exchange -> {
            requests.incrementAndGet();
            respond(exchange, 200,
                    "[{\"date\":\"2026-01-01\",\"localName\":\"Año Nuevo\",\"countryCode\":\"CO\"}]");
        });
        NagerDateHolidayClient client = new NagerDateHolidayClient(baseUrl());

        List<PublicHoliday> firstResult = client.findUpcomingHolidays("CO");
        List<PublicHoliday> cachedResult = client.findUpcomingHolidays("CO");

        assertEquals(firstResult, cachedResult);
        assertEquals(1, requests.get());
    }

    @Test
    void mapsAnEmptyExternalResponseToAnEmptyHolidayList() throws Exception {
        server.createContext("/NextPublicHolidays/CO", exchange -> respond(exchange, 200, "null"));
        NagerDateHolidayClient client = new NagerDateHolidayClient(baseUrl());

        List<PublicHoliday> holidays = client.findUpcomingHolidays("CO");

        assertEquals(List.of(), holidays);
    }

    @Test
    void returnsStaleCachedHolidaysWhenTheProviderBecomesUnavailable() throws Exception {
        server.createContext("/NextPublicHolidays/CO", exchange -> respond(exchange, 503, "unavailable"));
        NagerDateHolidayClient client = new NagerDateHolidayClient(baseUrl());
        List<PublicHoliday> cachedHolidays = List.of(
                new PublicHoliday(java.time.LocalDate.of(2026, 1, 1), "Año Nuevo", "CO"));
        putExpiredCacheEntry(client, "CO", cachedHolidays);

        List<PublicHoliday> holidays = client.findUpcomingHolidays("CO");

        assertEquals(cachedHolidays, holidays);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void putExpiredCacheEntry(NagerDateHolidayClient client, String countryCode,
            List<PublicHoliday> holidays) throws Exception {
        Field cacheField = NagerDateHolidayClient.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        Class<?> cacheEntryType = Class.forName(NagerDateHolidayClient.class.getName() + "$CachedHolidays");
        Constructor<?> constructor = cacheEntryType.getDeclaredConstructor(List.class, long.class);
        constructor.setAccessible(true);
        Object expiredEntry = constructor.newInstance(holidays, 0L);
        ((Map) cacheField.get(client)).put(countryCode, expiredEntry);
    }

    private String baseUrl() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
