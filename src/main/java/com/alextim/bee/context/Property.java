package com.alextim.bee.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Property {
    public static String TITLE_APP;
    public static String DETECTOR_APP;
    public static String MG_DETECTOR_APP = "MG";
    public static String PN_DETECTOR_APP = "PN";
    public static String USER_APP;
    public static String SUPER_USER = "RTC";
    public static Integer QUEUE_CAPACITY;
    public static String DATE_BUILD = "24 06 2025";
    public static String SOFTWARE_VERSION = "Версия ПО: 3.5";
    public static Integer TRANSFER_ID;
    public static String TRANSFER_IP;
    public static Integer TRANSFER_RCV_PORT;
    public static Integer TRANSFER_TR_PORT;
    public static Integer TRANSFER_RCV_BUFFER_SIZE;

    public static Boolean GEO_DATA_ENABLE;
    public static Integer GEO_DATA_DELAY;
    public static Float GEO_DATA_DELTA;
    public static Float GEO_DATA_START_LAT;
    public static Float GEO_DATA_START_LON;

    public static String COUNTER_NUMBER_FORMAT;
    public static Integer MEAS_DATA_NUMBER_SING_DIGITS;
    public static Integer ERROR_NUMBER_SING_DIGITS;
    public static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static DateTimeFormatter DATE_TIME_FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
}
