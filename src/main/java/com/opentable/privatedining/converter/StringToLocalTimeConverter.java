package com.opentable.privatedining.converter;

import com.mongodb.lang.Nullable;
import com.opentable.privatedining.common.Constant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.springframework.core.convert.converter.Converter;

public class StringToLocalTimeConverter implements Converter<String, LocalTime> {

    private final ZoneId timeZone;

    public StringToLocalTimeConverter(ZoneId timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public LocalTime convert(@Nullable String source) {
        if (source == null) {
            return null;
        }
        LocalTime utcTime = LocalTime.parse(source, Constant.FORMATTER);
        LocalDate date = LocalDate.now(ZoneOffset.UTC);
        ZonedDateTime utcZdt = ZonedDateTime.of(date, utcTime, ZoneOffset.UTC);
        ZonedDateTime localZdt = utcZdt.withZoneSameInstant(timeZone);
        return localZdt.toLocalTime();
    }

}
