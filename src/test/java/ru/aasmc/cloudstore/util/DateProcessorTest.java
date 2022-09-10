package ru.aasmc.cloudstore.util;

import org.junit.jupiter.api.Test;

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

}