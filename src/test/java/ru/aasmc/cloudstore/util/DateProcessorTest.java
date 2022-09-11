package ru.aasmc.cloudstore.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class DateProcessorTest {

    @Test
    public void testCorrectDate() {
        String date = "2022-05-28T21:12:01.000Z";
        assertTrue(DateProcessor.checkDate(date));

        date = "2022-05-28T21:12:01";
        assertTrue(DateProcessor.checkDate(date));

        date = "2022-05-28";
        assertTrue(DateProcessor.checkDate(date));

        date = "2022-05-28T21:12:01+07:21";
        assertTrue(DateProcessor.checkDate(date));

        date = "2022-05-28T21:12";
        assertTrue(DateProcessor.checkDate(date));
    }

    @Test
    public void testIncorrectDate() {
        final String date = "202-05-28T21:12:01.Z";
        assertFalse(DateProcessor.checkDate(date));
    }

    @Test
    public void testConvertToDate() {
        String date = "2022-05-28T21:12:01.000Z";
        LocalDateTime result = DateProcessor.toDate(date);
        LocalDateTime expected = LocalDateTime.of(
                LocalDate.of(2022, Month.MAY, 28),
                LocalTime.of(21, 12, 01)
        );
        assertEquals(expected, result);

        date = "2022-05-28T21:12:01";
        result = DateProcessor.toDate(date);
        assertEquals(expected, result);
    }



}