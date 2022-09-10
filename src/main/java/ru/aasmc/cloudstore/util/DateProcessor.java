package ru.aasmc.cloudstore.util;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateProcessor {

    public static boolean checkDate(String date) {
        try {
            DateTimeFormatter formatter;
            if (date.contains("Z") || date.contains(".")) {
                formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
            } else if (date.contains("T")) {
                formatter = DateTimeFormatter.ISO_DATE_TIME;
            } else {
                formatter = DateTimeFormatter.ISO_DATE;
            }
            formatter.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

}
