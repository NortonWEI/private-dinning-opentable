package com.opentable.privatedining.common;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class Constant {

    private Constant() {

    }

    public static final ZoneId ZONE_ID = ZoneId.systemDefault();
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final int BLOCK_INTERVAL_MIN = 30;
}
