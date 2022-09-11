package ru.aasmc.cloudstore.util;

import org.springframework.http.HttpStatus;
import ru.aasmc.cloudstore.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateProcessor {

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static LocalDateTime toDate(String date) {
        try {
            ZonedDateTime zoned = ZonedDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(date));
            return LocalDateTime.of(
                    LocalDate.of(zoned.getYear(), zoned.getMonth(), zoned.getDayOfMonth()),
                    LocalTime.of(zoned.getHour(), zoned.getMinute(), zoned.getSecond())
            );
        } catch (Exception e) {
            try {
                return LocalDateTime.from(
                        DateTimeFormatter.ISO_DATE_TIME.parse(date)
                );
            } catch (Exception ex) {
                throw new ValidationException(HttpStatus.BAD_REQUEST, "Validation Failed");
            }
        }
    }

    public static boolean checkDate(String date) {
        try {
            DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(date);
            return true;
        } catch (Exception e) {
            try {
                DateTimeFormatter.ISO_DATE_TIME.parse(date);
                return true;
            } catch (Exception ex) {
                try {
                    DateTimeFormatter.ISO_DATE.parse(date);
                    return true;
                } catch (Exception exception) {
                    return false;
                }
            }
        }
    }

}
