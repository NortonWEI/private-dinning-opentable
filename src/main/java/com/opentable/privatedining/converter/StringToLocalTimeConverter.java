package com.opentable.privatedining.converter;

import com.mongodb.lang.Nullable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.core.convert.converter.Converter;

public class StringToLocalTimeConverter implements Converter<String, LocalTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final ZoneId timeZone;

    public StringToLocalTimeConverter(ZoneId timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public LocalTime convert(@Nullable String source) {
        if (source == null) {
            return null;
        }
        LocalTime utcTime = LocalTime.parse(source, FORMATTER);
        LocalDate date = LocalDate.now(ZoneOffset.UTC); // attach date in UTC so DST rules apply correctly
        ZonedDateTime utcZdt = ZonedDateTime.of(date, utcTime, ZoneOffset.UTC);
        ZonedDateTime localZdt = utcZdt.withZoneSameInstant(timeZone);
        return localZdt.toLocalTime();
    }

}
