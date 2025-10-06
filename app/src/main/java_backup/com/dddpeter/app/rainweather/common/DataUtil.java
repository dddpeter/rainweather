package com.dddpeter.app.rainweather.common;


import java.time.LocalDateTime;

public class DataUtil {
    public static int NOW_HOUR = LocalDateTime.now().getHour();

    public static boolean isDay() {
        return NOW_HOUR >= 6 && NOW_HOUR <= 18;
    }
}
