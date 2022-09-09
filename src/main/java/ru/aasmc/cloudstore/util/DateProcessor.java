package ru.aasmc.cloudstore.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateProcessor {
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

    public static ZonedDateTime toDate(final String date) {
        return ZonedDateTime.parse(date, formatter);
    }

    public static String toString(final ZonedDateTime date) {
        return date.format(formatter);
    }

}
