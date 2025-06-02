package com.alextim.bee.service;

public class MilliSecondFormatter {

    private static final long MILLIS_IN_DAY = 86_400_000;
    private static final long MILLIS_IN_HOUR = 3_600_000;
    private static final long MILLIS_IN_MINUTE = 60_000;
    private static final long MILLIS_IN_SECOND = 1_000;

    public static String toString(long millis) {
        StringBuilder res = new StringBuilder();

        long days = millis / MILLIS_IN_DAY;
        if (days != 0) {
            res.append(days).append(". ");
            millis %= MILLIS_IN_DAY;
        }

        long hour = millis / MILLIS_IN_HOUR;
        if (hour != 0) {
            res.append(hour).append(":");
            millis %= MILLIS_IN_HOUR;
        }

        long minutes = millis / MILLIS_IN_MINUTE;
        if (minutes != 0) {
            res.append(minutes).append(":");
            millis %= MILLIS_IN_MINUTE;
        }

        long seconds = millis / MILLIS_IN_SECOND;
        if (seconds != 0) {
            res.append(seconds).append(".");
            millis %= MILLIS_IN_SECOND;
        }

        res.append(millis/100);

        return res.toString();
    }

}
