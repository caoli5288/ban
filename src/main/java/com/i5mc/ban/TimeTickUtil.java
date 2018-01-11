package com.i5mc.ban;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class TimeTickUtil {

    private static final Map<String, Integer> UNIT = ImmutableMap.of(
            "m", 60,
            "h", 3600,
            "d", 86400
    );

    private static final Pattern NUM = Pattern.compile("\\d+");
    private static final Pattern NUM_WITH_UNIT = Pattern.compile("\\d+[mhd]");

    public static long toTick(String input) {
        return toTime(input, TimeUnit.SECONDS) * 20;
    }

    public static long toTime(String input, TimeUnit unit) {
        if (NUM.matcher(input).matches()) {
            return unit.convert(Integer.parseInt(input), TimeUnit.MINUTES);
        }

        if (NUM_WITH_UNIT.matcher(input).matches()) {
            return unit.convert(Integer.parseInt(input.substring(0, input.length() - 1)) * UNIT.get(input.substring(input.length() - 1)), TimeUnit.SECONDS);
        }

        throw new IllegalArgumentException(input);
    }

}
