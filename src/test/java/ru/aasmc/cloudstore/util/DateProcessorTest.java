package ru.aasmc.cloudstore.util;

import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

class DateProcessorTest {

    @Test
    public void testCorrectToDateConversion() {
        final String date = "2022-05-28T21:12:01.000Z";
        ZonedDateTime result = DateProcessor.toDate(date);
        ZonedDateTime expected = ZonedDateTime.of(
                LocalDate.of(2022, Month.MAY, 28),
                LocalTime.of(21, 12, 1),
                ZoneId.of("Z")
        );
        System.out.println(result);
        assertEquals(expected, result);
    }

    @Test
    public void testIncorrectToDateConversion() {
        final String date = "2022-05-28T21:12:01.Z";
        assertThrows(DateTimeParseException.class, () -> {
            DateProcessor.toDate(date);
        });
    }

    @Test
    public void testCorrectToStringConversion() {
        ZonedDateTime toFormat = ZonedDateTime.of(
                LocalDate.of(2022, Month.MAY, 28),
                LocalTime.of(21, 12, 1),
                ZoneId.of("Z")
        );
        String result = DateProcessor.toString(toFormat);
        System.out.println(result);
        final String expected = "2022-05-28T21:12:01.000Z";
        assertEquals(expected, result);
    }

}