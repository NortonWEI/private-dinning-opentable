package com.opentable.privatedining.converter;

import com.mongodb.lang.Nullable;
import com.opentable.privatedining.common.Constant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.springframework.core.convert.converter.Converter;

public class LocalTimeToStringConverter implements Converter<LocalTime, String> {

    private final ZoneId timeZone;

    public LocalTimeToStringConverter(ZoneId timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String convert(@Nullable LocalTime source) {
        if (source == null) {
            return null;
        }
        LocalDate date = LocalDate.now(timeZone);
        ZonedDateTime localZdt = ZonedDateTime.of(date, source, timeZone);
        ZonedDateTime utcZdt = localZdt.withZoneSameInstant(ZoneOffset.UTC);
        return utcZdt.format(Constant.FORMATTER);
    }

}
