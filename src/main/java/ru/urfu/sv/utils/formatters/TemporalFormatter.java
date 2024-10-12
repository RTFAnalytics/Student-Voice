package ru.urfu.sv.utils.formatters;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.urfu.sv.model.domain.entity.ClassSession;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TemporalFormatter {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm");

    public static String formatToDate(Instant instant) {
        return dateFormatter.format(instant);
    }

    public static String formatToDateTime(TemporalAccessor dateTime) {
        return dateTimeFormatter.format(dateTime);
    }

    public static String formatToDate(TemporalAccessor dateTime) {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy").format(dateTime);
    }

    public static String formatToTime(TemporalAccessor dateTime) {
        return DateTimeFormatter.ofPattern("HH:mm").format(dateTime);
    }

    public static String formatToOffsetDateTime(LocalDate localDate) {
        return OffsetDateTime.of(localDate, LocalTime.MIN, ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static LocalDateTime fromLocalDateTimeString(String localDateTimeStr) {
        return LocalDateTime.parse(localDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static String instantToEkbDateTime(Instant instant){
        return dateTimeFormatter.format(instant.atOffset(ZoneOffset.ofHours(5)));
    }

    public static String formatToSessionDateTime(ClassSession session){
        return "%s: %s - %s".formatted(
                TemporalFormatter.formatToDate(session.getStartDateTime()),
                TemporalFormatter.formatToTime(session.getStartDateTime()),
                TemporalFormatter.formatToTime(session.getEndDateTime()));
    }
}
